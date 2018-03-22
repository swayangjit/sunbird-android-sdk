package org.ekstep.genieservices.config;

import com.google.gson.internal.LinkedTreeMap;

import org.ekstep.genieservices.BaseService;
import org.ekstep.genieservices.IAuthSession;
import org.ekstep.genieservices.IFrameworkService;
import org.ekstep.genieservices.ServiceConstants;
import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.GenieResponseBuilder;
import org.ekstep.genieservices.commons.IParams;
import org.ekstep.genieservices.commons.bean.Channel;
import org.ekstep.genieservices.commons.bean.ChannelDetailsRequest;
import org.ekstep.genieservices.commons.bean.Framework;
import org.ekstep.genieservices.commons.bean.FrameworkDetailsRequest;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.db.model.NoSqlModel;
import org.ekstep.genieservices.commons.utils.DateUtil;
import org.ekstep.genieservices.commons.utils.FileUtil;
import org.ekstep.genieservices.commons.utils.GsonUtil;
import org.ekstep.genieservices.commons.utils.StringUtil;
import org.ekstep.genieservices.config.network.ChannelDetailsAPI;
import org.ekstep.genieservices.config.network.FrameworkDetailsAPI;
import org.ekstep.genieservices.telemetry.TelemetryLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the implementation of the interface {@link IFrameworkService}
 *
 * @author indraja
 */
public class FrameworkServiceImpl extends BaseService implements IFrameworkService {

    private static final String TAG = FrameworkServiceImpl.class.getSimpleName();
    private static final String DB_KEY_CHANNEL_DETAILS = "channel_details_key";
    private static final String DB_KEY_FRAMEWORK_DETAILS = "framework_details_key";

    public FrameworkServiceImpl(AppContext appContext) {
        super(appContext);
    }

    @Override
    public GenieResponse<Channel> getChannelDetails(final ChannelDetailsRequest channelDetailsRequest) {
        String methodName = "getChannelDetails@FrameworkServiceImpl";
        Map<String, Object> params = new HashMap<>();
        params.put("request", GsonUtil.toJson(channelDetailsRequest));

        long expirationTime = getLongFromKeyValueStore(FrameworkConstants.PreferenceKey.CHANNEL_DETAILS_API_EXPIRATION_KEY);
        String channelId = channelDetailsRequest.getChannelId();
        if (expirationTime == 0) {
            initializeChannelDetails(channelId);
        } else if (hasExpired(expirationTime)) {
            refreshChannelDetails(channelId);
        }

        NoSqlModel refreshDetailsInDb = NoSqlModel.findByKey(mAppContext.getDBSession(), DB_KEY_CHANNEL_DETAILS + channelId);

        Channel channelDetails = null;
        if (refreshDetailsInDb != null) {
            LinkedTreeMap map = GsonUtil.fromJson(refreshDetailsInDb.getValue(), LinkedTreeMap.class);
            String result = GsonUtil.toJson(((Map) map.get("result")).get("channel"));
            channelDetails = GsonUtil.fromJson(result, Channel.class);
        }

        GenieResponse<Channel> response;
        if (channelDetails != null) {
            response = GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE);
            response.setResult(channelDetails);
            TelemetryLogger.logSuccess(mAppContext, response, TAG, methodName, params);
        } else {
            response = GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.NO_CHANNEL_DETAILS_FOUND, ServiceConstants.ErrorMessage.UNABLE_TO_FIND_CHANNEL_DETAILS, TAG);
            TelemetryLogger.logFailure(mAppContext, response, TAG, methodName, params, ServiceConstants.ErrorMessage.UNABLE_TO_FIND_CHANNEL_DETAILS);
        }
        return response;
    }

    private void initializeChannelDetails(String channelId) {
        String storedData = FileUtil.readFileFromClasspath(FrameworkConstants.ResourceFile.CHANNEL_DETAILS_JSON_FILE);
        if (!StringUtil.isNullOrEmpty(storedData)) {
            saveChannelDetails(storedData, channelId);
        }
        refreshChannelDetails(channelId);
    }

    private void saveChannelDetails(String response, String channelId) {
        LinkedTreeMap map = GsonUtil.fromJson(response, LinkedTreeMap.class);
        LinkedTreeMap result = (LinkedTreeMap) map.get("result");
        if (result != null) {
            Double ttl = (Double) result.get("ttl");
            saveDataExpirationTime(ttl, FrameworkConstants.PreferenceKey.CHANNEL_DETAILS_API_EXPIRATION_KEY);
            result.remove("ttl");
            String key = DB_KEY_CHANNEL_DETAILS + channelId;
            NoSqlModel channelDetails = NoSqlModel.build(mAppContext.getDBSession(), (String) key, GsonUtil.toJson(result));
            NoSqlModel channelDetailsInDb = NoSqlModel.findByKey(mAppContext.getDBSession(), String.valueOf(key));
            if (channelDetailsInDb != null) {
                channelDetails.update();
            } else {
                channelDetails.save();
            }
        }
    }

    private void refreshChannelDetails(final String channelId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ChannelDetailsAPI channelAPI = new ChannelDetailsAPI(mAppContext, channelId);
                GenieResponse genieResponse = channelAPI.get();
                if (genieResponse.getStatus()) {
                    String body = genieResponse.getResult().toString();
                    saveChannelDetails(body, channelId);
                }
            }
        }).start();
    }

    @Override
    public GenieResponse<Framework> getFrameworkDetails(FrameworkDetailsRequest frameworkDetailsRequest) {
        String methodName = "getFrameworkDetails@FrameworkServiceImpl";
        Map<String, Object> params = new HashMap<>();
        params.put("request", GsonUtil.toJson(frameworkDetailsRequest));

        long expirationTime = getLongFromKeyValueStore(FrameworkConstants.PreferenceKey.FRAMEWORK_DETAILS_API_EXPIRATION_KEY);
        String frameworkId = null;
        if (frameworkDetailsRequest.isDefaultFrameworkDetails()) {
            frameworkId = getDefaultFrameworkDetails();
        } else {
            frameworkId = frameworkDetailsRequest.getFrameworkId();
        }

        if (expirationTime == 0) {
            initializeFrameworkDetails(frameworkId);
        } else if (hasExpired(expirationTime)) {
            refreshFrameworkDetails(frameworkId);
        }

        NoSqlModel refreshDetailsInDb = NoSqlModel.findByKey(mAppContext.getDBSession(), DB_KEY_FRAMEWORK_DETAILS + frameworkId);

        Framework frameworkDetails = null;
        if (refreshDetailsInDb != null) {
            LinkedTreeMap map = GsonUtil.fromJson(refreshDetailsInDb.getValue(), LinkedTreeMap.class);
            String result = GsonUtil.toJson(((Map) map.get("result")).get("framework"));
            frameworkDetails = new Framework(result);
        }

        GenieResponse<Framework> response;
        if (frameworkDetails != null) {
            response = GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE);
            response.setResult(frameworkDetails);
            TelemetryLogger.logSuccess(mAppContext, response, TAG, methodName, params);
        } else {
            response = GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.NO_FRAMEWORK_DETAILS_FOUND, ServiceConstants.ErrorMessage.UNABLE_TO_FIND_FRAMEWORK_DETAILS, TAG);
            TelemetryLogger.logFailure(mAppContext, response, TAG, methodName, params, ServiceConstants.ErrorMessage.UNABLE_TO_FIND_FRAMEWORK_DETAILS);

        }
        return response;
    }

    /**
     * get default framework details
     *
     * @return
     */
    private String getDefaultFrameworkDetails() {
        ChannelDetailsRequest channelDetailsRequest = new ChannelDetailsRequest.Builder().forChannel(IParams.Key.CHANNEL_ID).build();
        GenieResponse<Channel> channelDetailsResponse = getChannelDetails(channelDetailsRequest);
        Channel channelDetails = channelDetailsResponse.getResult();
        return channelDetails.getDefaultFramework();
    }

    private void initializeFrameworkDetails(String frameworkId) {
        String storedData = FileUtil.readFileFromClasspath(FrameworkConstants.ResourceFile.FRAMEWORK_DETAILS_JSON_FILE);
        if (!StringUtil.isNullOrEmpty(storedData)) {
            saveFrameworkDetails(storedData, frameworkId);
        }
        refreshFrameworkDetails(frameworkId);
    }

    private void saveFrameworkDetails(String response, String frameworkId) {
        LinkedTreeMap map = GsonUtil.fromJson(response, LinkedTreeMap.class);
        Map result = ((LinkedTreeMap) map.get("result"));
        if (result != null) {
            Double ttl = (Double) result.get("ttl");
            saveDataExpirationTime(ttl, FrameworkConstants.PreferenceKey.FRAMEWORK_DETAILS_API_EXPIRATION_KEY);
            result.remove("ttl");
            String key = DB_KEY_FRAMEWORK_DETAILS + frameworkId;
            NoSqlModel frameworkDetails = NoSqlModel.build(mAppContext.getDBSession(), (String) key, GsonUtil.toJson(result));
            NoSqlModel frameworkDetailsInDb = NoSqlModel.findByKey(mAppContext.getDBSession(), String.valueOf(key));
            if (frameworkDetailsInDb != null) {
                frameworkDetails.update();
            } else {
                frameworkDetails.save();
            }
        }
    }

    private boolean hasExpired(long expirationTime) {
        Long currentTime = DateUtil.getEpochTime();
        return currentTime > expirationTime;
    }

    private void refreshFrameworkDetails(final String frameworkId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FrameworkDetailsAPI frameworkDetailsAPI = new FrameworkDetailsAPI(mAppContext, frameworkId);
                GenieResponse genieResponse = frameworkDetailsAPI.get();
                if (genieResponse.getStatus()) {
                    String body = genieResponse.getResult().toString();
                    saveFrameworkDetails(body, frameworkId);
                }
            }
        }).start();
    }

    private void saveDataExpirationTime(Double ttl, String key) {
        if (ttl != null) {
            long ttlInMilliSeconds = (long) (ttl * DateUtil.MILLISECONDS_IN_AN_HOUR);
            Long currentTime = DateUtil.getEpochTime();
            long expiration_time = ttlInMilliSeconds + currentTime;

            mAppContext.getKeyValueStore().putLong(key, expiration_time);
        }
    }

}