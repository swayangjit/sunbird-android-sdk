package org.ekstep.genieservices.async;

import org.ekstep.genieservices.GenieService;

/**
 * Created by mathew on 22/5/17.
 */

public class GenieAsyncService {

    private static GenieService sService;
    private static GenieAsyncService sGenieAsyncService;
    private static ConfigService sConfigService;
    private static SyncService sSyncService;
    private static TelemetryService sTelemetryService;
    private static UserService sUserService;
    private static TagService sTagService;
    private static NotificationService sNotificationService;

    public static GenieAsyncService getAsyncService() {
        if (sGenieAsyncService == null) {
            sGenieAsyncService = new GenieAsyncService();
        }
        return sGenieAsyncService;
    }

    public static void init(GenieService genieService) {
        sService = genieService;
        sGenieAsyncService = new GenieAsyncService();
    }

    public ConfigService getConfigService() {
        if (sConfigService == null) {
            sConfigService = new ConfigService(sService);
        }
        return sConfigService;
    }

    public UserService getUserProfileService() {
        if (sUserService == null) {
            sUserService = new UserService(sService);
        }
        return sUserService;
    }

    public SyncService getSyncService() {
        if (sSyncService == null) {
            sSyncService = new SyncService(sService);
        }
        return sSyncService;
    }

    public TelemetryService getTelemetryService() {
        if (sTelemetryService == null) {
            sTelemetryService = new TelemetryService(sService);
        }
        return sTelemetryService;
    }

    public TagService getTagService() {
        if (sTagService == null) {
            sTagService = new TagService(sService);
        }
        return sTagService;
    }

    public NotificationService getNotificationService() {
        if (sNotificationService == null) {
            sNotificationService = new NotificationService(sService);
        }
        return sNotificationService;
    }


}