package org.ekstep.genieservices;

/**
 * Created on 19/4/17.
 *
 * @author swayangjit
 */
public interface ServiceConstants {
    String SERVICE_ERROR = "SERVICE_ERROR";
    String INVALID_JSON = "invalid json";
    String UNABLE_TO_CREATE_ANONYMOUS_USER = "unable to create anonymous user";
    String SUCCESS_RESPONSE = "successful";
    String FAILED_RESPONSE = "failed";
    String ERROR_DELETING_A_USER = "Error when deleting user profile";

    String INVALID_USER = "INVALID_USER";
    String NO_USER_WITH_SPECIFIED_ID = "There is no user with specified id exists";
    String VALIDATION_ERROR = "VALIDATION_ERROR";
    String INVALID_PROFILE = "INVALID_PROFILE";
    String UNABLE_TO_FIND_PROFILE = "unable to find profile";

    String NOT_EXISTS = "NOT_EXISTS";
    String NO_CURRENT_USER = "There is no current user";

    /* Key Constants for all the key value store */
    String KEY_USER_SESSION = "session";

    String NEVER_SYNCED = "NEVER";
    String SYNC_PROMPT = "SYNC_PROMPT";

    interface FileExtension {
        String CONTENT = "ecar";
        String PROFILE = "epar";
        String TELEMETRY = "gsa";
        String APK = "apk";
    }

    interface PreferenceKey {
        //Sync service pref keys
        String SYNC_CONFIG_SHARED_PREFERENCE_KEY = "syncConfig";
        String LAST_SYNC_TIME = "lastSyncTime";
        String SYNC_FILE_SIZE = "SYNC_FILE_SIZE";

        //Download queue
        String DOWNLOAD_QUEUE = "download_queue";

        //Partner
        String KEY_PARTNER_ID = "partnerid";
        String KEY_ACTIVE_PARTNER_ID = "partner.activeid";
        String SHARED_PREF_SESSION_KEY = "partnersessionid";
        String SHARED_PREF_PARTNERSET_EPOCH = "partnerSET";
    }

    interface API {
        String LP_EXTENSION = "/%s/v2";
        String EP_EXTENSION = "/%s/v1";
        String ANALYTICS_EXTENSION = "/analytics";
    }

    interface Partner {
        String KEY_PARTNER_ID = "partnerid";
        String SHARED_PREF_SESSION_KEY = "partnersessionid";
        String MISSING_PARTNER_ID = "MISSING_PARTNER_ID";
        String MISSING_PUBLIC_KEY = "MISSING_PUBLIC_KEY";
        String INVALID_RSA_PUBLIC_KEY = "INVALID_RSA_PUBLIC_KEY";
    }

    interface Event {
        String ERROR_INVALID_EVENT = "Invalid event";
        String ERROR_INVALID_JSON = "Invalid Json";
    }

    interface Tags {
        String KEY_GENIE_TAGS = "GENIE_TAGS";
    }

    interface APIExecutionMode {
        String MODE_WIFI = "WIFI";
        String MODE_MDATA = "MDATA";
        String MODE_LOCAL = "LOCAL";
        String MODE_NO_NETWORK = "NO_NETWORK";
    }

    interface Telemetry {
        String CONTENT_IMPORT_SUB_TYPE = "ContentImport";
        String CONTENT_IMPORT_STAGE_ID = "ImportContent";
        String AUTO_SYNC_SUB_TYPE_INITIATED = "AutoSync-Initiated";
        String AUTO_SYNC_SUB_TYPE_SUCCESS = "AutoSync-Success";
        String AUTO_SYNC_STAGE_ID = "Genie-TelemetrySync";
        String TYPE_OTHER = "OTHER";
        String TYPE_TOUCH = "TOUCH";
        String SIZE_OF_DATA_IN_KB = "SizeOfDataInKB";
    }

    interface ErrorCode {
        String PROCESSING_ERROR = "PROCESSING_ERROR";
        String DATA_NOT_FOUND_ERROR = "NO_DATA_FOUND";
        String VALIDATION_ERROR = "VALIDATION_ERROR";
        String DB_ERROR = "DB_ERROR";
        String INVALID_PROFILE = "INVALID_PROFILE";
        String PROFILE_NOT_FOUND = "PROFILE_NOT_FOUND";
        String INVALID_USER = "INVALID_USER";

        //Partner
        String UNREGISTERED_PARTNER = "UNREGISTERED_PARTNER";
        String CATASTROPHIC_FAILURE = "CATASTROPHIC_FAILURE";
        String ENCRYPTION_FAILURE = "ENCRYPTION_FAILURE";
        String MISSING_PARTNER_ID = "MISSING_PARTNER_ID";
        String MISSING_PUBLIC_KEY = "MISSING_PUBLIC_KEY";
        String INVALID_RSA_PUBLIC_KEY = "INVALID_RSA_PUBLIC_KEY";

        //Content
        String NO_DATA_FOUND = "NO_DATA_FOUND";
        String INVALID_FILE = "INVALID_FILE";

    }

    interface ErrorMessage {
        String UNABLE_TO_FIND_PROFILE = "unable to find profile";
        String NO_USER_WITH_SPECIFIED_ID = "There is no user with specified id exists";
    }

    interface ContentAccessStatus {
        int NOT_PLAYED = 0;
        int VIEWED = 1;
        int FULLY_PLAYED = 2;
    }

    interface ContentStatus {
        String LIVE = "LIVE";
        String DRAFT = "DRAFT";
    }

    interface GeTransferEvent {
        String TRANSFER_DIRECTION_EXPORT = "EXPORT";
        String TRANSFER_DIRECTION_IMPORT = "IMPORT";
        String DATATYPE_TELEMETRY = "TELEMETRY";
        String DATATYPE_CONTENT = "CONTENT";
        String DATATYPE_EXPLODED_CONTENT = "EXPLODEDCONTENT";
        String DATATYPE_PROFILE = "PROFILE";
        String CONTENT_ITEMS_KEY = "contents";
        String FILE_SIZE = "FILE_SIZE";
        String FILE_TYPE = "FILE_TYPE";
    }

}