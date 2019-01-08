package org.ekstep.genieservices.profile.network;

import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.IParams;
import org.ekstep.genieservices.commons.network.SunbirdBaseAPI;
import org.ekstep.genieservices.commons.utils.GsonUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created on 6/3/18.
 * anil
 */

public class SearchLocationAPI extends SunbirdBaseAPI {
    private static final String TAG = SearchLocationAPI.class.getSimpleName();

    private static final String ENDPOINT = "location/search";

    private Map<String, Object> requestMap;


    public SearchLocationAPI(AppContext appContext, Map<String, Object> requestMap) {
        super(appContext,
                String.format(Locale.US, "%s/%s",
                        appContext.getParams().getString(IParams.Key.DATA_SERVICE_BASE_URL),
                        ENDPOINT),
                TAG);

        this.requestMap = requestMap;
    }

    @Override
    protected Map<String, String> getRequestHeaders() {
        return null;
    }

    @Override
    protected String createRequestData() {
        Map<String, Object> request = new HashMap<>();
        request.put("request", requestMap);
        return GsonUtil.toJson(request);
    }
}
