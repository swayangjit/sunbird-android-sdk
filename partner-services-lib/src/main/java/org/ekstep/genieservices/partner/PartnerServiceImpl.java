package org.ekstep.genieservices.partner;

import org.ekstep.genieservices.BaseService;
import org.ekstep.genieservices.IPartnerService;
import org.ekstep.genieservices.ServiceConstants;
import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.GenieResponseBuilder;
import org.ekstep.genieservices.commons.bean.GameData;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.bean.PartnerData;
import org.ekstep.genieservices.commons.bean.telemetry.GERegisterPartner;
import org.ekstep.genieservices.commons.bean.telemetry.GESendPartnerData;
import org.ekstep.genieservices.commons.bean.telemetry.GEStartPartnerSession;
import org.ekstep.genieservices.commons.bean.telemetry.GETerminatePartnerSession;
import org.ekstep.genieservices.commons.exception.EncryptionException;
import org.ekstep.genieservices.commons.utils.Base64;
import org.ekstep.genieservices.commons.utils.Crypto;
import org.ekstep.genieservices.commons.utils.DateUtil;
import org.ekstep.genieservices.commons.utils.Logger;
import org.ekstep.genieservices.partner.db.model.PartnerModel;
import org.ekstep.genieservices.partner.db.model.PartnerSessionModel;
import org.ekstep.genieservices.telemetry.TelemetryLogger;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * This is the implementation of the interface {@link IPartnerService}
 */

public class PartnerServiceImpl extends BaseService implements IPartnerService {

    public static final String TAG = PartnerServiceImpl.class.getSimpleName();

    private static final String TEST = "test";
    private AppContext appContext;
    private GameData mGameData = null;

    /**
     * Constructor of PartnerServiceImpl
     *
     * @param appContext
     */
    public PartnerServiceImpl(AppContext appContext) {
        super(appContext);
        this.appContext = appContext;
        this.mGameData = new GameData(mAppContext.getParams().getGid(), mAppContext.getParams().getVersionName());
    }

    @Override
    public GenieResponse<Void> registerPartner(PartnerData partnerData) {
        Logger.i(TAG, "REGISTERING Partner: " + partnerData.getPartnerID());
        GenieResponse<Void> response;

        PartnerModel partnerModel = PartnerModel.findByPartnerId(appContext.getDBSession(), partnerData.getPartnerID());
        if (partnerModel != null) {
            response = GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE, Void.class);
            return response;
        } else {
            List<String> errors = isValid(partnerData);
            if (errors.isEmpty()) {
                partnerModel = PartnerModel.build(appContext.getDBSession(), partnerData.getPartnerID(), partnerData.getPublicKey(), getPublicKeyId(partnerData));
                partnerModel.save();

                GERegisterPartner geRegisterPartner = new GERegisterPartner(mGameData, partnerData.getPartnerID(), partnerData.getPublicKey(), getPublicKeyId(partnerData), mAppContext.getDeviceInfo().getDeviceID());
                TelemetryLogger.log(geRegisterPartner);

                response = GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE, Void.class);
                TelemetryLogger.logSuccess(mAppContext, response, new HashMap(), TAG, "registerPartner@PartnerServiceImpl", new HashMap());
                return response;
            } else {
                String errorMessage = "INVALID_DATA";
                response = GenieResponseBuilder.getErrorResponse(ServiceConstants.FAILED_RESPONSE, errorMessage, TAG, Void.class);
                response.setErrorMessages(errors);

                TelemetryLogger.logFailure(mAppContext, response, TAG, "registerPartner@PartnerServiceImpl", new HashMap(), errorMessage);
                return response;
            }
        }
    }

    private String getPublicKeyId(PartnerData request) {
        if (request.getPublicKey() != null) {
            try {
                return Crypto.checksum(request.getPublicKey());
            } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                Logger.e(TAG, "Bad Algorithm", e);
                return null;
            }
        }
        return null;
    }

    @Override
    public GenieResponse<Boolean> isRegistered(String partnerID) {
        PartnerModel partnerModel = PartnerModel.findByPartnerId(appContext.getDBSession(), partnerID);
        GenieResponse<Boolean> response = GenieResponseBuilder.getSuccessResponse("", Boolean.class);
        if (partnerModel != null) {
            response.setResult(true);
        } else {
            response.setResult(false);
        }

        return response;
    }

    @Override
    public GenieResponse<Void> startPartnerSession(PartnerData partnerData) {
        Logger.i(TAG, "STARTING Partner Session" + partnerData.getPartnerID());
        GenieResponse<Void> response;
        PartnerModel partnerModel = PartnerModel.findByPartnerId(appContext.getDBSession(), partnerData.getPartnerID());

        if (partnerModel != null) {
            PartnerSessionModel partnerSessionModel = PartnerSessionModel.find(appContext);
            if (partnerSessionModel != null) {
                GETerminatePartnerSession geTerminatePartnerSession = new GETerminatePartnerSession(mGameData, partnerSessionModel.getPartnerID(), mAppContext.getDeviceInfo().getDeviceID(), DateUtil.getEpochDiff(partnerSessionModel.getEpochTime()), partnerSessionModel.getPartnerSessionId());
                TelemetryLogger.log(geTerminatePartnerSession);
                partnerSessionModel.clear();
            }
            partnerSessionModel = PartnerSessionModel.build(appContext, partnerData.getPartnerID());
            partnerSessionModel.save();

            GEStartPartnerSession geStartPartnerSession = new GEStartPartnerSession(mGameData, partnerSessionModel.getPartnerID(), mAppContext.getDeviceInfo().getDeviceID(), partnerSessionModel.getPartnerSessionId());
            TelemetryLogger.log(geStartPartnerSession);

            response = GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE, Void.class);
            TelemetryLogger.logSuccess(mAppContext, response, new HashMap(), TAG, "startPartnerSession@PartnerServiceImpl", new HashMap());
            return response;
        } else {
            String errorMessage = String.format(Locale.US, "- Session start failed! Partner: %s", partnerData.getPartnerID());
            response = GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.UNREGISTERED_PARTNER, errorMessage, TAG, Void.class);
            TelemetryLogger.logFailure(mAppContext, response, TAG, "startPartnerSession@PartnerServiceImpl", new HashMap(), errorMessage);
            return response;
        }
    }

    @Override
    public GenieResponse<Void> terminatePartnerSession(PartnerData partnerData) {
        String partnerID = partnerData.getPartnerID();
        Logger.i(TAG, "TERMINATING Partner Session" + partnerID);
        GenieResponse<Void> response;
        PartnerModel partnerModel = PartnerModel.findByPartnerId(appContext.getDBSession(), partnerID);
        PartnerSessionModel partnerSessionModel = PartnerSessionModel.find(appContext);
        if (partnerModel != null && partnerSessionModel != null && partnerID.equals(partnerSessionModel.getPartnerID())) {
            partnerSessionModel.clear();
            response = GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE, Void.class);
            TelemetryLogger.logSuccess(mAppContext, response, new HashMap(), TAG, "terminatePartnerSession@PartnerServiceImpl", new HashMap());
            return response;
        } else {
            String errorMessage = String.format(Locale.US, "- Session termination failed! Partner: %s", partnerID);
            response = GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.UNREGISTERED_PARTNER, errorMessage, TAG, Void.class);
            TelemetryLogger.logFailure(mAppContext, response, TAG, "terminatePartnerSession@PartnerServiceImpl", new HashMap(), errorMessage);
            return response;
        }
    }

    @Override
    public GenieResponse<String> sendData(PartnerData partnerData) {
        Logger.i(TAG, "SENDING Partner Data " + partnerData.getPartnerID());

        GenieResponse<String> response;
        PartnerModel partnerModel = PartnerModel.findByPartnerId(appContext.getDBSession(), partnerData.getPartnerID());

        if (partnerModel != null) {
            try {
                Map<String, String> data = processData(partnerData);
                GESendPartnerData geSendPartnerData = new GESendPartnerData(mGameData, partnerModel.getPartnerID(),
                        partnerModel.getPublicKeyId(), mAppContext.getDeviceInfo().getDeviceID(), data.get("encrypted_data"), data.get("encrypted_key"), data.get("iv"));
                TelemetryLogger.log(geSendPartnerData);
                response = GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE, String.class);
                response.setResult(data.toString());
                TelemetryLogger.logSuccess(mAppContext, response, new HashMap(), TAG, "terminatePartnerSession@PartnerServiceImpl", new HashMap());
                return response;
            } catch (EncryptionException e) {
                List<String> errorMessages = new ArrayList<>();
                String errorMessage = e.getMessage();
                errorMessages.add(errorMessage);
                String message = String.format(Locale.US, "- Encrypting data failed! Partner: %s", partnerData.getPartnerID());
                response = GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.ENCRYPTION_FAILURE, message, TAG, String.class);
                TelemetryLogger.logFailure(mAppContext, response, TAG, "sendData@PartnerServiceImpl", new HashMap(), message);
                return response;
            }
        } else {
            String errorMessage = String.format(Locale.US, "- Sending data failed! Partner: %s", partnerData.getPartnerID());
            response = GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.UNREGISTERED_PARTNER, errorMessage, TAG, String.class);
            TelemetryLogger.logFailure(mAppContext, response, TAG, "sendData@PartnerServiceImpl", new HashMap(), errorMessage);
            return response;
        }
    }


    private Map<String, String> processData(PartnerData partnerData) throws EncryptionException {
        Map<String, String> data = new HashMap<>();
        try {
            SecretKey AESKey = Crypto.generateAESKey(); //ok
            IvParameterSpec iv = Crypto.generateIVSpecForAES();
            String ivString = Base64.encodeToString(iv.getIV(), Base64.DEFAULT);
            PublicKey publicKey = Crypto.generatePublicKey(partnerData.getPublicKey());
            byte[] encryptedKey = Crypto.encryptSecretKeyWithRSAPublic(AESKey, publicKey);
            String encryptedKeyString = Base64.encodeToString(encryptedKey, Base64.DEFAULT);
            String encryptedData = Crypto.encryptWithAES(partnerData.getPartnerData(), AESKey, iv);
            data.put("iv", ivString);
            data.put("encrypted_data", encryptedData);
            data.put("encrypted_key", encryptedKeyString);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchPaddingException | UnsupportedEncodingException | InvalidAlgorithmParameterException e) {
            throw new EncryptionException(e.getMessage());
        }
        return data;
    }

    //TODO This should be moved into the bean class using the ivalidate interface
    private List<String> isValid(PartnerData request) {
        List<String> errorMessages = new ArrayList<String>();
        if (request.getPartnerID() == null || request.getPartnerID().isEmpty())
            errorMessages.add(ServiceConstants.ErrorCode.MISSING_PARTNER_ID);
        if (request.getPublicKey() == null || request.getPublicKey().isEmpty())
            errorMessages.add(ServiceConstants.ErrorCode.MISSING_PUBLIC_KEY);
        try {
            Crypto.encryptWithPublic(TEST, request.getPublicKey());
            Crypto.checksum(request.getPublicKey());
        } catch (Exception e) {
            Logger.e(TAG, "ERROR Bad Public Key!", e);
            errorMessages.add(ServiceConstants.ErrorCode.INVALID_RSA_PUBLIC_KEY);
        }
        return errorMessages;
    }

}