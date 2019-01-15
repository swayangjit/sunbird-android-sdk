package org.ekstep.genieservices.profile;

import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.bean.AcceptTermsAndConditionsRequest;
import org.ekstep.genieservices.commons.bean.EndorseOrAddSkillRequest;
import org.ekstep.genieservices.commons.bean.GenerateOTPRequest;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.bean.LocationSearchCriteria;
import org.ekstep.genieservices.commons.bean.ProfileVisibilityRequest;
import org.ekstep.genieservices.commons.bean.Session;
import org.ekstep.genieservices.commons.bean.UpdateUserInfoRequest;
import org.ekstep.genieservices.commons.bean.UploadFileRequest;
import org.ekstep.genieservices.commons.bean.UserExistRequest;
import org.ekstep.genieservices.commons.bean.UserSearchCriteria;
import org.ekstep.genieservices.commons.bean.VerifyOTPRequest;
import org.ekstep.genieservices.commons.db.model.NoSqlModel;
import org.ekstep.genieservices.commons.utils.CollectionUtil;
import org.ekstep.genieservices.commons.utils.StringUtil;
import org.ekstep.genieservices.profile.network.AcceptTermsAndConditionsAPI;
import org.ekstep.genieservices.profile.network.EndorseOrAddSkillAPI;
import org.ekstep.genieservices.profile.network.FileUploadAPI;
import org.ekstep.genieservices.profile.network.GenerateOtpAPI;
import org.ekstep.genieservices.profile.network.GetUserByKeyAPI;
import org.ekstep.genieservices.profile.network.ProfileSkillsAPI;
import org.ekstep.genieservices.profile.network.ProfileVisibilityAPI;
import org.ekstep.genieservices.profile.network.SearchLocationAPI;
import org.ekstep.genieservices.profile.network.SearchUserAPI;
import org.ekstep.genieservices.profile.network.TenantInfoAPI;
import org.ekstep.genieservices.profile.network.UpdateUserInfoAPI;
import org.ekstep.genieservices.profile.network.UserProfileDetailsAPI;
import org.ekstep.genieservices.profile.network.VerifyOtpAPI;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 5/3/18.
 *
 * @author anil
 */
public class UserProfileHandler {


    private static Map<String, String> getCustomHeaders(Session authSession) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Authenticated-User-Token", authSession.getAccessToken());
        return headers;
    }

    public static GenieResponse fetchUserProfileDetailsFromServer(AppContext appContext, Session sessionData,
                                                                  String userId, String fields) {
        UserProfileDetailsAPI userProfileDetailsAPI = new UserProfileDetailsAPI(appContext,
                getCustomHeaders(sessionData), userId, fields);
        return userProfileDetailsAPI.get();
    }

    public static void refreshUserProfileDetailsFromServer(final AppContext appContext, final Session sessionData,
                                                           final String userId, final String fields,
                                                           final NoSqlModel userProfileInDB) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                GenieResponse userProfileDetailsAPIResponse = fetchUserProfileDetailsFromServer(appContext,
                        sessionData, userId, fields);
                if (userProfileDetailsAPIResponse.getStatus()) {
                    String jsonResponse = userProfileDetailsAPIResponse.getResult().toString();
                    if (!StringUtil.isNullOrEmpty(jsonResponse)) {
                        userProfileInDB.setValue(jsonResponse);
                        userProfileInDB.update();
                    }
                }
            }
        }).start();
    }

    public static GenieResponse fetchTenantInfoFromServer(AppContext appContext, Session sessionData, String slug) {
        TenantInfoAPI tenantInfoAPI = new TenantInfoAPI(appContext, getCustomHeaders(sessionData), slug);
        return tenantInfoAPI.get();
    }

    public static void refreshTenantInfoFromServer(final AppContext appContext, final Session sessionData,
                                                   final String slug, final NoSqlModel tenantInfoInDB) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                GenieResponse tenantInfoAPIResponse = fetchTenantInfoFromServer(appContext, sessionData, slug);
                if (tenantInfoAPIResponse.getStatus()) {
                    String jsonResponse = tenantInfoAPIResponse.getResult().toString();
                    if (!StringUtil.isNullOrEmpty(jsonResponse)) {
                        tenantInfoInDB.setValue(jsonResponse);
                        tenantInfoInDB.update();
                    }
                }
            }
        }).start();
    }

    public static GenieResponse setProfileVisibilityDetailsInServer(AppContext appContext, Session sessionData,
                                                                    ProfileVisibilityRequest profileVisibilityRequest) {
        ProfileVisibilityAPI profileVisibilityAPI = new ProfileVisibilityAPI(appContext, getCustomHeaders(sessionData),
                getProfileVisibilityRequest(profileVisibilityRequest));
        return profileVisibilityAPI.post();
    }

    private static Map<String, Object> getProfileVisibilityRequest(ProfileVisibilityRequest profileVisibilityRequest) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("userId", profileVisibilityRequest.getUserId());

        if (profileVisibilityRequest.getPrivateFields() != null) {
            requestMap.put("private", profileVisibilityRequest.getPrivateFields());
        }

        if (profileVisibilityRequest.getPublicFields() != null) {
            requestMap.put("public", profileVisibilityRequest.getPublicFields());
        }

        return requestMap;
    }

    public static GenieResponse searchUser(AppContext appContext, Session sessionData, UserSearchCriteria userSearchCriteria) {
        SearchUserAPI searchUserAPI = new SearchUserAPI(appContext, getCustomHeaders(sessionData),
                getSearchUserParameters(userSearchCriteria));
        return searchUserAPI.post();
    }

    private static Map<String, Object> getSearchUserParameters(UserSearchCriteria userSearchCriteria) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("query", userSearchCriteria.getQuery());
        requestMap.put("offset", userSearchCriteria.getOffset());
        requestMap.put("limit", userSearchCriteria.getLimit());
        if (userSearchCriteria.getIdentifiers() != null) {
            Map<String, Object> identifiersMap = new HashMap<>();
            identifiersMap.put("identifier", userSearchCriteria.getIdentifiers());
            requestMap.put("filters", identifiersMap);
        }
        return requestMap;
    }

    public static GenieResponse fetchProfileSkillsFromServer(AppContext appContext, Session sessionData) {
        ProfileSkillsAPI profileSkillsAPI = new ProfileSkillsAPI(appContext, getCustomHeaders(sessionData));
        return profileSkillsAPI.get();
    }

    public static void refreshProfileSkillsFromServer(final AppContext appContext, final Session sessionData,
                                                      final NoSqlModel profileSkillsInDB) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                GenieResponse profileSkillsAPIResponse = fetchProfileSkillsFromServer(appContext, sessionData);
                if (profileSkillsAPIResponse.getStatus()) {
                    String jsonResponse = profileSkillsAPIResponse.getResult().toString();
                    if (!StringUtil.isNullOrEmpty(jsonResponse)) {
                        profileSkillsInDB.setValue(jsonResponse);
                        profileSkillsInDB.update();
                    }
                }
            }
        }).start();
    }

    public static GenieResponse endorseOrAddSkillsInServer(AppContext appContext, Session sessionData,
                                                           EndorseOrAddSkillRequest endorseOrAddSkillRequest) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("endorsedUserId", endorseOrAddSkillRequest.getUserId());
        requestMap.put("skillName", endorseOrAddSkillRequest.getSkills());

        EndorseOrAddSkillAPI endorseOrAddSkillAPI = new EndorseOrAddSkillAPI(appContext,
                getCustomHeaders(sessionData), requestMap);
        return endorseOrAddSkillAPI.post();
    }

    public static GenieResponse uploadFile(AppContext appContext, Session sessionData, UploadFileRequest uploadFileRequest) {
        FileUploadAPI fileUploadAPI = new FileUploadAPI(appContext, getCustomHeaders(sessionData),
                getFileUploadParameters(uploadFileRequest));
        return fileUploadAPI.post();
    }

    private static Map<String, Object> getFileUploadParameters(UploadFileRequest uploadFileRequest) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("file", uploadFileRequest.getFile());
        requestMap.put("container", uploadFileRequest.getUserId());

        return requestMap;
    }

    public static GenieResponse updateUserInfoInServer(AppContext appContext, Session sessionData, UpdateUserInfoRequest updateUserInfoRequest) {
        UpdateUserInfoAPI updateUserInfoAPI = new UpdateUserInfoAPI(appContext, getCustomHeaders(sessionData),
                getUpdateUserInfoRequestMap(updateUserInfoRequest));
        return updateUserInfoAPI.patch();
    }

    private static Map<String, Object> getUpdateUserInfoRequestMap(UpdateUserInfoRequest updateUserInfoRequest) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("userId", updateUserInfoRequest.getUserId());

        if (!StringUtil.isNullOrEmpty(updateUserInfoRequest.getFirstName())) {
            requestMap.put("firstName", updateUserInfoRequest.getFirstName());
        }

        if (!StringUtil.isNullOrEmpty(updateUserInfoRequest.getLastName())) {
            requestMap.put("lastName", updateUserInfoRequest.getLastName());
        }

        if (!CollectionUtil.isEmpty(updateUserInfoRequest.getLanguage())) {
            requestMap.put("language", updateUserInfoRequest.getLanguage());
        }

        if (updateUserInfoRequest.isPhoneVerified()
                && !StringUtil.isNullOrEmpty(updateUserInfoRequest.getPhone())) {
            requestMap.put("phone", updateUserInfoRequest.getPhone());
            requestMap.put("phoneVerified", updateUserInfoRequest.isPhoneVerified());
        }

        if (updateUserInfoRequest.isEmailVerified()
                && !StringUtil.isNullOrEmpty(updateUserInfoRequest.getEmail())) {
            requestMap.put("email", updateUserInfoRequest.getEmail());
            requestMap.put("emailVerified", updateUserInfoRequest.isEmailVerified());
        }

        if (!StringUtil.isNullOrEmpty(updateUserInfoRequest.getProfileSummary())) {
            requestMap.put("profileSummary", updateUserInfoRequest.getProfileSummary());
        }

        if (!CollectionUtil.isEmpty(updateUserInfoRequest.getSubject())) {
            requestMap.put("subject", updateUserInfoRequest.getSubject());
        }

        if (!StringUtil.isNullOrEmpty(updateUserInfoRequest.getGender())) {
            requestMap.put("gender", updateUserInfoRequest.getGender());
        }

        if (!StringUtil.isNullOrEmpty(updateUserInfoRequest.getDob())) {
            requestMap.put("dob", updateUserInfoRequest.getDob());
        }

        if (!CollectionUtil.isEmpty(updateUserInfoRequest.getGrade())) {
            requestMap.put("grade", updateUserInfoRequest.getGrade());
        }

        if (!StringUtil.isNullOrEmpty(updateUserInfoRequest.getLocation())) {
            requestMap.put("location", updateUserInfoRequest.getLocation());
        }

        if (!CollectionUtil.isEmpty(updateUserInfoRequest.getLocationCodes())) {
            requestMap.put("locationCodes", updateUserInfoRequest.getLocationCodes());
        }

        if (!StringUtil.isNullOrEmpty(updateUserInfoRequest.getAvatar())) {
            requestMap.put("avatar", updateUserInfoRequest.getAvatar());
        }

        if (!CollectionUtil.isNullOrEmpty(updateUserInfoRequest.getWebPages())) {
            requestMap.put("webPages", updateUserInfoRequest.getWebPages());
        }

        if (!CollectionUtil.isNullOrEmpty(updateUserInfoRequest.getEducation())) {
            requestMap.put("education", updateUserInfoRequest.getEducation());
        }

        if (!CollectionUtil.isNullOrEmpty(updateUserInfoRequest.getJobProfile())) {
            requestMap.put("jobProfile", updateUserInfoRequest.getJobProfile());
        }

        if (!CollectionUtil.isNullOrEmpty(updateUserInfoRequest.getAddress())) {
            requestMap.put("address", updateUserInfoRequest.getAddress());
        }

        if (updateUserInfoRequest.getFramework() != null && !updateUserInfoRequest.getFramework().isEmpty()) {
            requestMap.put("framework", updateUserInfoRequest.getFramework());
        }

        return requestMap;
    }

    public static GenieResponse acceptTermsAndConditions(AppContext appContext, Session sessionData,
                                                         AcceptTermsAndConditionsRequest acceptTermsAndConditionsRequest) {
        AcceptTermsAndConditionsAPI acceptTermsAndConditionsAPI = new AcceptTermsAndConditionsAPI(appContext, getCustomHeaders(sessionData),
                getAcceptTermsAndConditionsRequestMap(acceptTermsAndConditionsRequest));
        return acceptTermsAndConditionsAPI.post();
    }

    private static Map<String, Object> getAcceptTermsAndConditionsRequestMap(AcceptTermsAndConditionsRequest acceptTermsAndConditionsRequest) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("version", acceptTermsAndConditionsRequest.getVersion());

        return requestMap;
    }

    public static GenieResponse isAlreadyInUse(AppContext appContext, UserExistRequest userExistRequest) {
        GetUserByKeyAPI getUserByKeyAPI = new GetUserByKeyAPI(appContext, userExistRequest.getKey(), userExistRequest.getType());
        return getUserByKeyAPI.get();
    }

    public static GenieResponse generateOTP(AppContext appContext, GenerateOTPRequest generateOTPRequest) {
        GenerateOtpAPI generateOtpAPI = new GenerateOtpAPI(appContext, generateOtpAPIRequestMap(generateOTPRequest));
        return generateOtpAPI.post();
    }

    private static Map<String, Object> generateOtpAPIRequestMap(GenerateOTPRequest generateOTPRequest) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("key", generateOTPRequest.getKey());
        requestMap.put("type", generateOTPRequest.getType());

        return requestMap;
    }

    public static GenieResponse verifyOTP(AppContext appContext, VerifyOTPRequest verifyOTPRequest) {
        VerifyOtpAPI generateOtpAPI = new VerifyOtpAPI(appContext, verifyOtpAPIRequestMap(verifyOTPRequest));
        return generateOtpAPI.post();
    }

    private static Map<String, Object> verifyOtpAPIRequestMap(VerifyOTPRequest verifyOTPRequest) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("key", verifyOTPRequest.getKey());
        requestMap.put("type", verifyOTPRequest.getType());
        requestMap.put("otp", verifyOTPRequest.getOtp());

        return requestMap;
    }

    public static GenieResponse searchLocation(AppContext appContext, LocationSearchCriteria locationSearchCriteria) {
        SearchLocationAPI searchLocationAPI = new SearchLocationAPI(appContext, getSearchLocationParameters(locationSearchCriteria));
        return searchLocationAPI.post();
    }

    private static Map<String, Object> getSearchLocationParameters(LocationSearchCriteria locationSearchCriteria) {
        Map<String, Object> filterMap = new HashMap<>();
        if (!StringUtil.isNullOrEmpty(locationSearchCriteria.getType())) {
            filterMap.put("type", locationSearchCriteria.getType());
        }

        if (!StringUtil.isNullOrEmpty(locationSearchCriteria.getParentId())) {
            filterMap.put("parentId", locationSearchCriteria.getParentId());
        }

        if (!StringUtil.isNullOrEmpty(locationSearchCriteria.getCode())) {
            filterMap.put("code", locationSearchCriteria.getCode());
        }

        Map<String, Object> requestMap = new HashMap<>();
//        requestMap.put("query", locationSearchCriteria.getQuery());
//        requestMap.put("offset", locationSearchCriteria.getOffset());
//        requestMap.put("limit", locationSearchCriteria.getLimit());
        requestMap.put("filters", filterMap);

        return requestMap;
    }
}
