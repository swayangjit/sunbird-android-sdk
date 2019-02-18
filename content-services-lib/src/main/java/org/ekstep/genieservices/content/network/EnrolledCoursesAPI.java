package org.ekstep.genieservices.content.network;

import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.IParams;
import org.ekstep.genieservices.commons.network.SunbirdBaseAPI;

import java.util.Locale;
import java.util.Map;

/**
 * Created on 9/3/18.
 *
 * @author anil
 */
public class EnrolledCoursesAPI extends SunbirdBaseAPI {

    private static final String TAG = EnrolledCoursesAPI.class.getSimpleName();

    private static final CharSequence SERVICE_ENDPOINTS = "user/enrollment/list";

    private Map<String, String> headers;

    public EnrolledCoursesAPI(AppContext appContext, Map<String, String> customHeaders, String userId) {
        super(appContext,
                String.format(Locale.US, "%s/%s/%s?batchDetails=name,endDate,startDate,status",
                        appContext.getParams().getString(IParams.Key.COURSE_SERVICE_BASE_URL),
                        SERVICE_ENDPOINTS,
                        userId),
                TAG);

        this.headers = customHeaders;
    }

    @Override
    protected Map<String, String> getRequestHeaders() {
        return this.headers;
    }

    @Override
    protected String createRequestData() {
        return null;
    }
}
