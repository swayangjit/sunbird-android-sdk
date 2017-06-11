package org.ekstep.genieservices.telemetry.chained;

import org.ekstep.genieservices.ServiceConstants;
import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.GenieResponseBuilder;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.bean.ImportContext;
import org.ekstep.genieservices.commons.chained.IChainable;
import org.ekstep.genieservices.telemetry.model.ProcessedEventModel;
import org.ekstep.genieservices.telemetry.model.ProcessedEventsModel;

/**
 * Created on 6/8/2017.
 *
 * @author anil
 */
public class TransportProcessedEventsImportEvent implements IChainable {

    private static final String TAG = TransportProcessedEventsImportEvent.class.getSimpleName();
    private IChainable nextLink;

    @Override
    public GenieResponse<Void> execute(AppContext appContext, ImportContext importContext) {
        ProcessedEventsModel processedEventsModel = ProcessedEventsModel.find(importContext.getDBSession());
        if (processedEventsModel != null) {
            for (ProcessedEventModel model : processedEventsModel.getProcessedEventList()) {
                ProcessedEventModel processedEventModel = ProcessedEventModel.build(appContext.getDBSession(), model.getMsgId(), model.getData(), model.getNumberOfEvents(), model.getPriority());
                processedEventModel.save();
            }
        }

        if (nextLink != null) {
            return nextLink.execute(appContext, importContext);
        } else {
            return GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.IMPORT_FAILED, "Import telemetry failed.", TAG);
        }
    }

    @Override
    public IChainable then(IChainable link) {
        nextLink = link;
        return link;
    }
}