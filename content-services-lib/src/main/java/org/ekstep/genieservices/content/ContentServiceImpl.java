package org.ekstep.genieservices.content;

import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import org.ekstep.genieservices.BaseService;
import org.ekstep.genieservices.IConfigService;
import org.ekstep.genieservices.IContentFeedbackService;
import org.ekstep.genieservices.IContentService;
import org.ekstep.genieservices.IUserService;
import org.ekstep.genieservices.ServiceConstants;
import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.GenieResponseBuilder;
import org.ekstep.genieservices.commons.bean.Content;
import org.ekstep.genieservices.commons.bean.ContentAccess;
import org.ekstep.genieservices.commons.bean.ContentAccessCriteria;
import org.ekstep.genieservices.commons.bean.ContentCriteria;
import org.ekstep.genieservices.commons.bean.ContentSearchCriteria;
import org.ekstep.genieservices.commons.bean.ContentSearchResult;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.bean.MasterData;
import org.ekstep.genieservices.commons.bean.MasterDataValues;
import org.ekstep.genieservices.commons.bean.Profile;
import org.ekstep.genieservices.commons.bean.enums.ContentType;
import org.ekstep.genieservices.commons.bean.enums.MasterDataType;
import org.ekstep.genieservices.commons.utils.FileHandler;
import org.ekstep.genieservices.commons.utils.GsonUtil;
import org.ekstep.genieservices.commons.utils.Logger;
import org.ekstep.genieservices.commons.utils.StringUtil;
import org.ekstep.genieservices.content.bean.ImportContext;
import org.ekstep.genieservices.content.chained.AddGeTransferContentImportEvent;
import org.ekstep.genieservices.content.chained.ContentImportStep;
import org.ekstep.genieservices.content.chained.DeviceMemoryCheck;
import org.ekstep.genieservices.content.chained.EcarCleanUp;
import org.ekstep.genieservices.content.chained.ExtractEcar;
import org.ekstep.genieservices.content.chained.ExtractPayloads;
import org.ekstep.genieservices.content.chained.IChainable;
import org.ekstep.genieservices.content.chained.ValidateEcar;
import org.ekstep.genieservices.content.db.model.ContentModel;
import org.ekstep.genieservices.content.db.model.ContentsModel;
import org.ekstep.genieservices.content.network.ContentSearchAPI;
import org.ekstep.genieservices.content.network.RecommendedContentAPI;
import org.ekstep.genieservices.content.network.RelatedContentAPI;
import org.ekstep.genieservices.content.utils.ContentHandler;
import org.ekstep.genieservices.content.utils.ContentUtil;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Created on 5/10/2017.
 *
 * @author anil
 */
public class ContentServiceImpl extends BaseService implements IContentService {

    private static final String TAG = ContentServiceImpl.class.getSimpleName();

    private IUserService userService;
    private IContentFeedbackService contentFeedbackService;
    private IConfigService configService;

    public ContentServiceImpl(AppContext appContext, IUserService userService, IContentFeedbackService contentFeedbackService, IConfigService configService) {
        super(appContext);

        this.userService = userService;
        this.contentFeedbackService = contentFeedbackService;
        this.configService = configService;
    }

    @Override
    public GenieResponse<Content> getContentDetails(String contentIdentifier) {
        // TODO: Telemetry logger
        String methodName = "getContentDetails@ContentServiceImpl";

        GenieResponse<Content> response;
        ContentModel contentModelInDB = ContentModel.find(mAppContext.getDBSession(), contentIdentifier);

        if (contentModelInDB == null) {     // Fetch from server if detail is not available in DB
            Map contentData = ContentHandler.fetchContentDetails(mAppContext, contentIdentifier);
            if (contentData == null) {
                response = GenieResponseBuilder.getErrorResponse(ServiceConstants.NO_DATA_FOUND, "No content found for identifier = " + contentIdentifier, TAG);
                return response;
            }

            contentModelInDB = ContentModel.build(mAppContext.getDBSession(), contentData, null);
        } else {
            ContentHandler.refreshContentDetails(mAppContext, contentIdentifier, contentModelInDB);
        }

        Content content = ContentHandler.getContent(contentModelInDB, true, true, contentFeedbackService, userService);

        response = GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE);
        response.setResult(content);
        return response;
    }

    @Override
    public GenieResponse<List<Content>> getAllLocalContent(ContentCriteria criteria) {
        // TODO: Telemetry logger
        String methodName = "getAllLocalContent@ContentServiceImpl";

        GenieResponse<List<Content>> response;
        if (criteria == null) {
            criteria = new ContentCriteria();
        }

        List<ContentModel> contentModelListInDB = ContentHandler.getAllLocalContentModel(mAppContext, criteria);

        String uid;
        if (!StringUtil.isNullOrEmpty(criteria.getUid())) {
            uid = criteria.getUid();
        } else {
            uid = ContentHandler.getCurrentUserId(userService);
        }

        // Get the content access for profile.
        List<ContentAccess> contentAccessList = ContentHandler.getAllContentAccessByUid(userService, uid);

        List<Content> contentList = new ArrayList<>();
        for (ContentAccess contentAccess : contentAccessList) {
            ContentModel contentModel = ContentModel.find(mAppContext.getDBSession(), contentAccess.getIdentifier());
            if (contentModel != null && contentModelListInDB.contains(contentModel)) {
                Content c = ContentHandler.getContent(contentModel, criteria.isAttachFeedback(), criteria.isAttachContentAccess(), contentFeedbackService, userService);
                c.setContentAccess(contentAccess);
                contentList.add(c);
                contentModelListInDB.remove(contentModel);
            }
        }

        // Add the remaining content into list
        for (ContentModel contentModel : contentModelListInDB) {
            Content c = ContentHandler.getContent(contentModel, criteria.isAttachFeedback(), criteria.isAttachContentAccess(), contentFeedbackService, userService);
            contentList.add(c);
        }

        response = GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE);
        response.setResult(contentList);
        return response;
    }

    @Override
    public GenieResponse<List<Content>> getChildContents(String contentIdentifier, int levelAndState) {
        // TODO: Telemetry logger
        String methodName = "getChildContents@ContentServiceImpl";

        GenieResponse<List<Content>> response;
        ContentModel contentModel = ContentModel.find(mAppContext.getDBSession(), contentIdentifier);
        if (contentModel == null) {
            response = GenieResponseBuilder.getErrorResponse(ServiceConstants.NO_DATA_FOUND, "No content found for identifier = " + contentIdentifier, TAG);
            return response;
        }

        List<Content> childContentList = new ArrayList<>();

        switch (levelAndState) {
            case ContentConstants.ChildContents.FIRST_LEVEL_ALL:
                childContentList = getAllChildContents(content, childContents);
                break;

            case ContentConstants.ChildContents.FIRST_LEVEL_DOWNLOADED:
                childContentList = populateChildren(content, childContents);
                break;

            case ContentConstants.ChildContents.FIRST_LEVEL_SPINE:
                childContentList = populateChildren(content, childContents);
                break;

            default:
        }

        response = GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE);
        response.setResult(childContentList);
        return response;
    }

    @Override
    public GenieResponse<Void> deleteContent(String contentIdentifier, int level) {
        GenieResponse<Void> response;
        ContentModel contentModel = ContentModel.find(mAppContext.getDBSession(), contentIdentifier);

        if (contentModel == null) {
            response = GenieResponseBuilder.getErrorResponse(ServiceConstants.NO_DATA_FOUND, "No content found to delete for identifier = " + contentIdentifier, TAG);
            return response;
        }

        // TODO: Removing external content code
//        if (contentModel.isExternalContent() && mAppContext.getDeviceInfo().getAndroidSdkVersion() <= mAppContext.getDeviceInfo().getKitkatVersionCode()) {
//            response = GenieResponseBuilder.getErrorResponse(ServiceConstants.FAILED_RESPONSE, "This content cannot be deleted.", TAG);
//            return response;
//        }

        if (contentModel.hasPreRequisites()) {
            List<String> preRequisitesIdentifier = contentModel.getPreRequisitesIdentifiers();
            ContentsModel contentsModel = ContentsModel.findAllContentsWithIdentifiers(mAppContext.getDBSession(), preRequisitesIdentifier);

            if (contentsModel != null) {
                for (ContentModel c : contentsModel.getContentModelList()) {
                    deleteOrUpdateContent(c, true, level);
                }
            }
        }

        //delete or update child items
        if (contentModel.hasChildren()) {
            deleteAllChild(contentModel, level);
        }

        //delete or update parent items
        deleteOrUpdateContent(contentModel, false, level);

        // TODO: Removing external content code
//        if (contentModel.isExternalContent()) {
//            FileHandler.refreshSDCard();
//        }

        response = GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE);
        return response;
    }

    private void deleteAllChild(ContentModel contentModel, int level) {
        Queue<ContentModel> queue = new LinkedList<>();

        queue.add(contentModel);

        ContentModel node;
        while (!queue.isEmpty()) {
            node = queue.remove();

            if (node.hasChildren()) {
                List<String> childContentsIdentifiers = node.getChildContentsIdentifiers();
                ContentsModel contentsModel = ContentsModel.findAllContentsWithIdentifiers(mAppContext.getDBSession(), childContentsIdentifiers);
                if (contentsModel != null) {
                    queue.addAll(contentsModel.getContentModelList());
                }
            }

            // Deleting only child content
            if (!contentModel.getIdentifier().equalsIgnoreCase(node.getIdentifier())) {
                deleteOrUpdateContent(node, true, level);
            }
        }
    }

    private void deleteOrUpdateContent(ContentModel contentModel, boolean isChildItems, int level) {

        int refCount = contentModel.getRefCount();

        if (level == ContentConstants.Delete.NESTED) {
            // If visibility is Default it means this content was visible in my downloads.
            // After deleting artifact for this content it should not visible as well so reduce the refCount also for this.
            if (refCount > 1 && ContentConstants.Visibility.DEFAULT.equalsIgnoreCase(contentModel.getVisibility())) {
                refCount = refCount - 1;

                // Update visibility
                contentModel.setVisibility(ContentConstants.Visibility.PARENT);
            }

            // Update the contentState
            // Do not update the content state if contentType is Collection / TextBook / TextBookUnit
            if (ContentType.COLLECTION.getValue().equalsIgnoreCase(contentModel.getContentType())
                    || ContentType.TEXTBOOK.getValue().equalsIgnoreCase(contentModel.getContentType())
                    || ContentType.TEXTBOOK_UNIT.getValue().equalsIgnoreCase(contentModel.getContentType())) {
                contentModel.addOrUpdateContentState(ContentConstants.State.ARTIFACT_AVAILABLE);
            } else {
                contentModel.addOrUpdateContentState(ContentConstants.State.ONLY_SPINE);

                // if there are no entry in DB for any content then on this case contentModel.getPath() will be null
                if (contentModel.getPath() != null) {
                    FileHandler.rm(new File(contentModel.getPath()), contentModel.getIdentifier());
                }
            }

        } else {
            // TODO: This check should be before updating the existing refCount.
            // Do not update the content state if contentType is Collection / TextBook / TextBookUnit and refCount is more than 1.
            if ((ContentType.COLLECTION.getValue().equalsIgnoreCase(contentModel.getContentType())
                    || ContentType.TEXTBOOK.getValue().equalsIgnoreCase(contentModel.getContentType())
                    || ContentType.TEXTBOOK_UNIT.getValue().equalsIgnoreCase(contentModel.getContentType()))
                    && refCount > 1) {
                contentModel.addOrUpdateContentState(ContentConstants.State.ARTIFACT_AVAILABLE);
            } else if (refCount > 1 && isChildItems) {  //contentModel.isVisibilityDefault() &&
                // Visibility will remain Default only.

                contentModel.addOrUpdateContentState(ContentConstants.State.ARTIFACT_AVAILABLE);
            } else {

                // Set the visibility to Parent so that this content will not visible in My contents / Downloads section.
                // Update visibility
                if (ContentConstants.Visibility.DEFAULT.equalsIgnoreCase(contentModel.getVisibility())) {
                    contentModel.setVisibility(ContentConstants.Visibility.PARENT);
                }

                contentModel.addOrUpdateContentState(ContentConstants.State.ONLY_SPINE);

                // if there are no entry in DB for any content then on this case contentModel.getPath() will be null
                if (contentModel.getPath() != null) {
                    FileHandler.rm(new File(contentModel.getPath()), contentModel.getIdentifier());
                }
            }

            refCount = refCount - 1;
        }

        // Update the refCount
        contentModel.addOrUpdateRefCount(refCount);

        // if there are no entry in DB for any content then on this case contentModel.getPath() will be null
        if (contentModel.getPath() != null) {
            contentModel.update();
        }
    }

    @Override
    public GenieResponse<ContentSearchResult> searchContent(ContentSearchCriteria contentSearchCriteria) {
        GenieResponse<ContentSearchResult> response;

        Map<String, String[]> filter = contentSearchCriteria.getFilter();
        // Apply profile specific filter
        if (userService != null) {
            GenieResponse<Profile> profileResponse = userService.getCurrentUser();
            if (profileResponse.getStatus()) {
                Profile currentProfile = profileResponse.getResult();

                // Add age filter
                applyFilter(MasterDataType.AGEGROUP, String.valueOf(currentProfile.getAge()), filter);

                // Add board filter
                if (currentProfile.getBoard() != null) {
                    applyFilter(MasterDataType.BOARD, currentProfile.getBoard(), filter);
                }

                // Add medium filter
                if (currentProfile.getMedium() != null) {
                    applyFilter(MasterDataType.MEDIUM, currentProfile.getMedium(), filter);
                }

                // Add standard filter
                applyFilter(MasterDataType.GRADELEVEL, String.valueOf(currentProfile.getStandard()), filter);
            }
        }

        // Apply partner specific filer
        // TODO: 5/15/2017 - Uncomment after partner getting the API for getPartnerInfo in PartnerService.
//        HashMap<String, String> partnerInfo = GsonUtil.fromJson(getSharedPreferenceWrapper().getString(Constants.KEY_PARTNER_INFO, null), HashMap.class);
//        if (partnerInfo != null) {
//            //Apply Channel filter
//            String channel = partnerInfo.get(Constant.BUNDLE_KEY_PARTNER_CHANNEL);
//            if (channel != null) {
//                applyFilter(MasterDataType.CHANNEL, channel, filter);
//            }
//
//            //Apply Purpose filter
//            String purpose = partnerInfo.get(Constant.BUNDLE_KEY_PARTNER_PURPOSE);
//            if (purpose != null) {
//                applyFilter(MasterDataType.PURPOSE, purpose, filter);
//            }
//        }

        contentSearchCriteria.setFilter(filter);

        // Populating implicit search criteria.
        List<String> facets = contentSearchCriteria.getFacets();
        facets.addAll(Arrays.asList("contentType", "domain", "ageGroup", "language", "gradeLevel"));
        contentSearchCriteria.setFacets(facets);

        addFiltersIfNotAvailable(contentSearchCriteria, "objectType", Arrays.asList("Content"));
        addFiltersIfNotAvailable(contentSearchCriteria, "contentType", Arrays.asList("Story", "Worksheet", "Collection", "Game", "TextBook"));
        addFiltersIfNotAvailable(contentSearchCriteria, "status", Arrays.asList("Live"));

        ContentSearchAPI contentSearchAPI = new ContentSearchAPI(mAppContext, getRequest(contentSearchCriteria));
        GenieResponse apiResponse = contentSearchAPI.post();
        if (apiResponse.getStatus()) {
            String body = apiResponse.getResult().toString();

            LinkedTreeMap map = GsonUtil.fromJson(body, LinkedTreeMap.class);
            String id = (String) map.get("id");
            LinkedTreeMap responseParams = (LinkedTreeMap) map.get("params");
            LinkedTreeMap result = (LinkedTreeMap) map.get("result");
            String responseFacetsString = GsonUtil.toJson(result.get("facets"));
            String contentDataListString = GsonUtil.toJson(result.get("content"));

            Type type = new TypeToken<List<HashMap<String, Object>>>() {
            }.getType();
            List<Map<String, Object>> responseFacets = GsonUtil.getGson().fromJson(responseFacetsString, type);
            List<Map<String, Object>> contentDataList = GsonUtil.getGson().fromJson(contentDataListString, type);

            List<Content> contents = new ArrayList<>();
            for (Map contentDataMap : contentDataList) {
                // TODO: 5/15/2017 - Can fetch content from DB and return in response.
                ContentModel contentModel = ContentModel.build(mAppContext.getDBSession(), contentDataMap, null);
                Content content = ContentHandler.getContent(contentModel, false, false, contentFeedbackService, userService);
                contents.add(content);
            }

            ContentSearchResult searchResult = new ContentSearchResult();
            searchResult.setId(id);
            searchResult.setParams(responseParams);
            searchResult.setFacets(getSortedFacets(responseFacets));
            searchResult.setRequest(getRequest(contentSearchCriteria));
            searchResult.setContents(contents);

            response = GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE);
            response.setResult(searchResult);
            return response;
        }

        response = GenieResponseBuilder.getErrorResponse(apiResponse.getError(), (String) apiResponse.getErrorMessages().get(0), TAG);
        return response;
    }

    // TODO: TO be done by Swayangjit
    private void applyFilter(MasterDataType masterDataType, String propertyValue, Map<String, String[]> filter) {
        try {

            if (masterDataType == MasterDataType.AGEGROUP) {
                masterDataType = MasterDataType.AGE;
            }

            MasterDataValues masterDataValues = null;
            if (configService != null) {
                GenieResponse<MasterData> masterDataResponse = configService.getMasterData(masterDataType);

                MasterData masterData = null;
                if (masterDataResponse.getStatus()) {
                    masterData = masterDataResponse.getResult();
                }

                for (MasterDataValues values : masterData.getValues()) {
                    if (values.getValue().equals(propertyValue)) {
                        masterDataValues = values;
                        break;
                    }
                }
            }
            Map termMap = (Map) map.get(propertyValue);

            String masterDataTypeValue = masterDataType.getValue();

            Set termSet = new HashSet((List) termMap.get(masterDataTypeValue));
            if (filter.containsKey(masterDataTypeValue)) {
                if (filter.get(masterDataTypeValue) != null) {
                    Set set = new HashSet(Arrays.asList(filter.get(masterDataTypeValue)));
                    if (set != null && termSet != null) {
                        termSet.addAll(set);
                    }
                }
            }

            String[] strArr = new String[termSet.size()];
            termSet.toArray(strArr);
            filter.put(masterDataTypeValue, strArr);
        } catch (Exception e) {
            Logger.e(TAG, "Failed to apply filter");
        }
    }

    private void addFiltersIfNotAvailable(ContentSearchCriteria contentSearchCriteria, String key, List<String> values) {
        Map<String, String[]> filter = contentSearchCriteria.getFilter();
        if (filter == null) {
            filter = new HashMap<>();
        }

        if (filter.isEmpty() || filter.get(key) == null) {
            String[] newValues = values.toArray(new String[values.size()]);
            filter.put(key, newValues);
        }

        contentSearchCriteria.setFilter(filter);
    }

    private HashMap<String, Object> getRequest(ContentSearchCriteria criteria) {
        HashMap<String, Object> request = new HashMap<>();
        request.put("query", criteria.getQuery());
        request.put("limit", criteria.getLimit());
        request.put("mode", "soft");

        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put("compatibilityLevel", getCompatibilityLevel());
        if (criteria.getFilter() != null) {
            filterMap.putAll(criteria.getFilter());
        }
        request.put("filters", filterMap);

        if (criteria.getSort() != null) {
            request.put("sort_by", criteria.getSort());
        }

        if (criteria.getFacets() != null) {
            request.put("facets", criteria.getFacets());
        }

        return request;
    }

    private HashMap<String, Integer> getCompatibilityLevel() {
        HashMap<String, Integer> compatLevelMap = new HashMap<>();
        compatLevelMap.put("max", ContentUtil.maxCompatibilityLevel);
        compatLevelMap.put("min", ContentUtil.minCompatibilityLevel);
        return compatLevelMap;
    }

    private List<Map<String, Object>> getSortedFacets(List<Map<String, Object>> facets) {
        if (configService == null) {
            return facets;
        }

        GenieResponse<Map<String, Object>> ordinalsResponse = configService.getOrdinals();
        if (ordinalsResponse.getStatus()) {

            Map<String, Object> ordinalsMap = ordinalsResponse.getResult();

            if (ordinalsMap != null) {
                List<Map<String, Object>> sortedFacetList = new ArrayList<>();
                for (Map<String, Object> facetMap : facets) {
                    for (String nameKey : facetMap.keySet()) {
                        if (nameKey.equals("name")) {
                            String facetName = (String) facetMap.get(nameKey);

                            String facetValuesString = GsonUtil.toJson(facetMap.get("values"));
                            Type facetType = new TypeToken<List<Map<String, Object>>>() {
                            }.getType();
                            List<Map<String, Object>> facetValues = GsonUtil.getGson().fromJson(facetValuesString, facetType);

                            if (ordinalsMap.containsKey(facetName)) {
                                String dataString = GsonUtil.toJson(ordinalsMap.get(facetName));
                                Type type = new TypeToken<List<String>>() {
                                }.getType();
                                List<String> facetsOrder = GsonUtil.getGson().fromJson(dataString, type);

                                List<Map<String, Object>> valuesList = sortOrder(facetValues, facetsOrder);

                                HashMap<String, Object> map = new HashMap<>();
                                map.put("name", facetName);
                                map.put("values", valuesList);

                                sortedFacetList.add(map);
                            }
                            break;
                        }
                    }
                }

                return sortedFacetList;
            }
        }

        return facets;
    }

    private List<Map<String, Object>> sortOrder(List<Map<String, Object>> facetValues, List<String> facetsOrder) {
        Map<Integer, Map<String, Object>> map = new TreeMap<>();

        for (Map<String, Object> value : facetValues) {
            String name = (String) value.get("name");
            int index = indexOf(facetsOrder, name);
            map.put(index, value);
        }

        List<Map<String, Object>> valuesList = new ArrayList<>(map.values());
        return valuesList;
    }

    private int indexOf(List<String> responseFacets, String key) {
        if (!StringUtil.isNullOrEmpty(key)) {
            for (int i = 0; i < responseFacets.size(); i++) {
                if (key.equalsIgnoreCase(responseFacets.get(i))) {
                    return i;
                }
            }
        }

        return -1;
    }

    @Override
    public GenieResponse<ContentSearchResult> getRecommendedContent(String language) {
//        HashMap params = new HashMap();
//        params.put("mode", getNetworkMode());
        String method = "getRecommendedContents@ContentServiceImpl";

        GenieResponse<ContentSearchResult> response;
        RecommendedContentAPI recommendedContentAPI = new RecommendedContentAPI(mAppContext, getRecommendedContentRequest(language));
        GenieResponse apiResponse = recommendedContentAPI.post();
        if (apiResponse.getStatus()) {
            String body = apiResponse.getResult().toString();

            LinkedTreeMap map = GsonUtil.fromJson(body, LinkedTreeMap.class);
            String id = (String) map.get("id");
            LinkedTreeMap responseParams = (LinkedTreeMap) map.get("params");
            LinkedTreeMap result = (LinkedTreeMap) map.get("result");
            String contentDataListString = GsonUtil.toJson(result.get("content"));

            Type type = new TypeToken<List<HashMap<String, Object>>>() {
            }.getType();
            List<Map<String, Object>> contentDataList = GsonUtil.getGson().fromJson(contentDataListString, type);

            List<Content> contents = new ArrayList<>();
            for (Map contentDataMap : contentDataList) {
                // TODO: 5/15/2017 - Can fetch content from DB and return in response.
                ContentModel contentModel = ContentModel.build(mAppContext.getDBSession(), contentDataMap, null);
                Content content = ContentHandler.getContent(contentModel, false, false, contentFeedbackService, userService);
                contents.add(content);
            }

            ContentSearchResult searchResult = new ContentSearchResult();
            searchResult.setId(id);
            searchResult.setParams(responseParams);
            searchResult.setContents(contents);

            response = GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE);
            response.setResult(searchResult);
            return response;
        }

        response = GenieResponseBuilder.getErrorResponse(apiResponse.getError(), (String) apiResponse.getErrorMessages().get(0), TAG);
        return response;
    }

    private HashMap<String, Object> getRecommendedContentRequest(String language) {
        HashMap<String, Object> contextMap = new HashMap<>();
        contextMap.put("did", mAppContext.getDeviceInfo().getDeviceID());
        contextMap.put("dlang", language);

        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("context", contextMap);
        requestMap.put("limit", 10);

        return requestMap;
    }

    @Override
    public GenieResponse<ContentSearchResult> getRelatedContent(String contentIdentifier) {
        // TODO: 5/18/2017 - Telemetry
//        HashMap params = new HashMap();
//        params.put("uid", uid);
//        params.put("content_id", contentIdentifier);
//        params.put("mode", getNetworkMode());
//        String method = "getRelatedContent@ContentServiceImpl";

        GenieResponse<ContentSearchResult> response;
        RelatedContentAPI relatedContentAPI = new RelatedContentAPI(mAppContext, getRelatedContentRequest(contentIdentifier));
        GenieResponse apiResponse = relatedContentAPI.post();
        if (apiResponse.getStatus()) {
            String body = apiResponse.getResult().toString();

            LinkedTreeMap map = GsonUtil.fromJson(body, LinkedTreeMap.class);
            String id = (String) map.get("id");
            LinkedTreeMap responseParams = (LinkedTreeMap) map.get("params");
            LinkedTreeMap result = (LinkedTreeMap) map.get("result");
            String contentDataListString = GsonUtil.toJson(result.get("content"));

            Type type = new TypeToken<List<HashMap<String, Object>>>() {
            }.getType();
            List<Map<String, Object>> contentDataList = GsonUtil.getGson().fromJson(contentDataListString, type);

            List<ContentModel> allLocalContentModel = ContentHandler.getAllLocalContentModel(mAppContext, new ContentCriteria());

            List<Content> contents = new ArrayList<>();
            for (Map contentDataMap : contentDataList) {
                ContentModel contentModel = ContentModel.build(mAppContext.getDBSession(), contentDataMap, null);
                Content content = ContentHandler.getContent(contentModel, false, false, contentFeedbackService, userService);

                if (allLocalContentModel.contains(contentModel)) {
                    content.setAvailableLocally(true);
                }

                contents.add(content);
            }

            ContentSearchResult searchResult = new ContentSearchResult();
            searchResult.setId(id);
            searchResult.setParams(responseParams);
            searchResult.setContents(contents);

            response = GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE);
            response.setResult(searchResult);
            return response;
        }

        response = GenieResponseBuilder.getErrorResponse(apiResponse.getError(), (String) apiResponse.getErrorMessages().get(0), TAG);
        return response;
    }

    private HashMap<String, Object> getRelatedContentRequest(String contentIdentifier) {
        String dlang = "";
        String uid = "";
        if (userService != null) {
            GenieResponse<Profile> profileGenieResponse = userService.getCurrentUser();
            if (profileGenieResponse.getStatus()) {
                Profile profile = profileGenieResponse.getResult();
                uid = profile.getUid();
                dlang = profile.getLanguage();
            }
        }

        HashMap<String, Object> contextMap = new HashMap<>();
        contextMap.put("did", mAppContext.getDeviceInfo().getDeviceID());
        contextMap.put("dlang", dlang);
        contextMap.put("contentid", contentIdentifier);
        contextMap.put("uid", uid);

        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("context", contextMap);
        requestMap.put("limit", 10);

        return requestMap;
    }

    @Override
    public GenieResponse<List<Content>> nextContent(List<String> contentIdentifiers) {
        List<Content> contentList = new ArrayList<>();

        try {
            List<String> contentsKeyList = new ArrayList<>();
            List<String> parentChildRelation = new ArrayList<>();
            String key = null;

            ContentModel contentModel = ContentModel.find(mAppContext.getDBSession(), contentIdentifiers.get(0));

            Stack<ContentModel> stack = new Stack<>();
            stack.push(contentModel);

            ContentModel node;
            while (!stack.isEmpty()) {
                node = stack.pop();
                if (node.hasChildren()) {
                    List<ContentModel> childContents = ContentUtil.getSortedChildrenList(mAppContext.getDBSession(), node.getLocalData(), ContentConstants.ChildContents.FIRST_LEVEL_ALL);
                    // TODO: 5/19/2017 -      List<ContentModel> childContents = node.getSortedChildrenList(dbOperator, CHILD_CONTENTS_FIRST_LEVEL_ALL);
                    stack.addAll(childContents);

                    for (ContentModel c : childContents) {
                        parentChildRelation.add(node.getIdentifier() + "/" + c.getIdentifier());
                    }
                }

                if (StringUtil.isNullOrEmpty(key)) {
                    key = node.getIdentifier();

                    // First content
//                contents.put(key, node);

                } else {
                    String tempKey;

                    for (int i = key.split("/").length - 1; i >= 0; i--) {
                        String immediateParent = key.split("/")[i];

                        if (parentChildRelation.contains(immediateParent + "/" + node.getIdentifier())) {
                            break;
                        } else {
                            key = key.substring(0, key.lastIndexOf("/"));
                        }
                    }

                    if (ContentType.COLLECTION.getValue().equalsIgnoreCase(node.getContentType())
                            || ContentType.TEXTBOOK.getValue().equalsIgnoreCase(node.getContentType())
                            || ContentType.TEXTBOOK_UNIT.getValue().equalsIgnoreCase(node.getContentType())) {
                        key = key + "/" + node.getIdentifier();

                    } else {
                        tempKey = key + "/" + node.getIdentifier();

                        contentsKeyList.add(tempKey);
                    }
                }
            }

            String currentIdentifiers = null;
            for (String identifier : contentIdentifiers) {
                if (StringUtil.isNullOrEmpty(currentIdentifiers)) {
                    currentIdentifiers = identifier;
                } else {
                    currentIdentifiers = currentIdentifiers + "/" + identifier;
                }
            }

            int indexOfCurrentContentIdentifier = contentsKeyList.indexOf(currentIdentifiers);
            String nextContentIdentifier = null;
            if (indexOfCurrentContentIdentifier > 0) {
                nextContentIdentifier = contentsKeyList.get(indexOfCurrentContentIdentifier - 1);
            }

            if (!StringUtil.isNullOrEmpty(nextContentIdentifier)) {

                String nextContentIdentifierList[] = nextContentIdentifier.split("/");
                for (String identifier : nextContentIdentifierList) {
                    ContentModel nextContentModel = ContentModel.find(mAppContext.getDBSession(), identifier);

                    Content content = ContentHandler.getContent(nextContentModel, false, false, contentFeedbackService, userService);
                    contentList.add(content);
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "" + e.getMessage());
        }

        GenieResponse<List<Content>> response = GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE);
        response.setResult(contentList);
        return response;
    }

    @Override
    public GenieResponse<Void> importContent(boolean isChildContent, String ecarFilePath) {
        // TODO: 5/16/2017 - Telemetry logger
        String method = "importContent@ContentServiceImpl";
        Map<String, Object> params = new HashMap<>();
        params.put("importContent", ecarFilePath);
        params.put("isChildContent", isChildContent);
        params.put("logLevel", "2");

        GenieResponse<Void> response;

        if (!FileHandler.doesFileExists(ecarFilePath)) {
            response = GenieResponseBuilder.getErrorResponse(ContentConstants.INVALID_FILE, "content import failed, file doesn't exists", TAG);
            return response;
        }

        String ext = FileHandler.getFileExt(ecarFilePath);
        if (!ServiceConstants.FileExtension.CONTENT.equals(ext)) {
            response = GenieResponseBuilder.getErrorResponse(ContentConstants.INVALID_FILE, "content import failed, unsupported file extension", TAG);
            return response;
        } else {
            File ecarFile = new File(ecarFilePath);
            File tmpLocation = new File(FileHandler.getTmpDir(mAppContext.getPrimaryFilesDir()), UUID.randomUUID().toString());
            ImportContext importContext = new ImportContext(ecarFile, tmpLocation);

            IChainable importContentSteps = ContentImportStep.initImportContent();
            importContentSteps.then(new DeviceMemoryCheck())
                    .then(new ExtractEcar())
                    .then(new ValidateEcar())
                    .then(new ExtractPayloads())
                    .then(new EcarCleanUp())
                    .then(new AddGeTransferContentImportEvent());

            return importContentSteps.execute(mAppContext, importContext);
        }
    }

}