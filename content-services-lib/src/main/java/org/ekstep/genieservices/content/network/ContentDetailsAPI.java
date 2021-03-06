package org.ekstep.genieservices.content.network;

import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.IParams;
import org.ekstep.genieservices.commons.network.BaseAPI;

import java.util.Locale;
import java.util.Map;

/**
 * Created on 5/12/2017.
 *
 * @author anil
 */
public class ContentDetailsAPI extends BaseAPI {

    private static final String TAG = ContentDetailsAPI.class.getSimpleName();

    private static final CharSequence SERVICE_ENDPOINTS = "read";

    public ContentDetailsAPI(AppContext appContext, String contentId) {
        super(appContext,
                String.format(Locale.US, "%s/%s/%s",
                        appContext.getParams().getString(IParams.Key.CONTENT_BASE_URL), SERVICE_ENDPOINTS,
                        contentId),
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
