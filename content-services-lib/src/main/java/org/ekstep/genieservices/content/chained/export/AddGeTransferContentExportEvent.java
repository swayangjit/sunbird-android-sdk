package org.ekstep.genieservices.content.chained.export;

import org.ekstep.genieservices.ServiceConstants;
import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.GenieResponseBuilder;
import org.ekstep.genieservices.commons.bean.ContentExportResponse;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.bean.ImportContext;
import org.ekstep.genieservices.commons.bean.telemetry.GETransfer;
import org.ekstep.genieservices.commons.bean.telemetry.GETransferEventKnowStructure;
import org.ekstep.genieservices.commons.bean.telemetry.GETransferMap;
import org.ekstep.genieservices.commons.chained.IChainable;
import org.ekstep.genieservices.content.ContentHandler;
import org.ekstep.genieservices.telemetry.TelemetryLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created on 6/19/2017.
 *
 * @author anil
 */
public class AddGeTransferContentExportEvent implements IChainable<ContentExportResponse> {

    @Override
    public GenieResponse<ContentExportResponse> execute(AppContext appContext, ImportContext importContext) {
        Map<String, Object> metadata = importContext.getMetadata();

        GETransferEventKnowStructure eks = new GETransferEventKnowStructure(
                GETransferEventKnowStructure.TRANSFER_DIRECTION_EXPORT,
                GETransferEventKnowStructure.DATATYPE_CONTENT,
                ((List) metadata.get(GETransferEventKnowStructure.CONTENT_ITEMS_KEY)).size(),
                (Long) metadata.get(GETransferEventKnowStructure.FILE_SIZE),
                buildContentsMetadata(importContext.getItems()));
        GETransfer geTransfer = new GETransfer(eks);
        TelemetryLogger.log(geTransfer);

        ContentExportResponse contentExportResponse = new ContentExportResponse();
        contentExportResponse.setExportedFilePath(importContext.getEcarFile().toString());

        GenieResponse<ContentExportResponse> response = GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE);
        response.setResult(contentExportResponse);
        return response;
    }

    @Override
    public IChainable<ContentExportResponse> then(IChainable<ContentExportResponse> link) {
        return link;
    }

    private List<GETransferMap> buildContentsMetadata(List<Map<String, Object>> items) {
        List<GETransferMap> contentsMetadata = new ArrayList<>();

        for (Map item : items) {
            contentsMetadata.add(GETransferMap.createMapForContent(
                    ContentHandler.readIdentifier(item),
                    ContentHandler.readPkgVersion(item),
                    ContentHandler.readTransferCountFromContentMap(item),
                    ContentHandler.readOriginFromContentMap(item)));
        }

        return contentsMetadata;
    }
}
