package org.ekstep.genieservices.profile.network;

import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.IParams;
import org.ekstep.genieservices.commons.network.SunbirdBaseAPI;

import java.util.Locale;
import java.util.Map;

/**
 * Created on 08/01/19.
 *
 * @author anil
 */
public class GetUserByKeyAPI extends SunbirdBaseAPI {

    private static final String TAG = GetUserByKeyAPI.class.getSimpleName();

    private static final CharSequence SERVICE_ENDPOINTS = "get";

    public GetUserByKeyAPI(AppContext appContext, String key, String type) {
        super(appContext,
                String.format(Locale.US, "%s/%s/%s/%s",
                        appContext.getParams().getString(IParams.Key.USER_SERVICE_BASE_URL),
                        SERVICE_ENDPOINTS, type, key),
                TAG);
    }

    @Override
    protected Map<String, String> getRequestHeaders() {
        return null;
    }

    @Override
    protected String createRequestData() {
        return null;
    }
}
