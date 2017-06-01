package org.ekstep.genieresolvers.language;

import android.content.Context;

import org.ekstep.genieresolvers.BaseService;
import org.ekstep.genieservices.commons.IResponseHandler;

/**
 * Created on 24/5/17.
 * shriharsh
 */

public class LanguageService extends BaseService {
    private String appQualifier;
    private Context context;

    public LanguageService(Context context, String appQualifier) {
        this.context = context;
        this.appQualifier = appQualifier;
    }

    public void getTraversalRule(String languageId, IResponseHandler responseHandler) {
        GetTraversalRuleTask getTraversalRuleTask = new GetTraversalRuleTask(context, appQualifier, languageId);
        createAndExecuteTask(responseHandler, getTraversalRuleTask);
    }

    public void getLanguageSearch(String searchRequest, IResponseHandler responseHandler) {
        GetLanguageSearchTask getLanguageSearchTask = new GetLanguageSearchTask(context, appQualifier, searchRequest);
        createAndExecuteTask(responseHandler, getLanguageSearchTask);
    }

}