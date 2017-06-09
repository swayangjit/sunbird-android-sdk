package org.ekstep.genieservices.importexport;

import android.content.Context;

import org.ekstep.genieservices.IUserService;
import org.ekstep.genieservices.ServiceConstants;
import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.GenieResponseBuilder;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.bean.ProfileImportRequest;
import org.ekstep.genieservices.commons.db.operations.IDBSession;
import org.ekstep.genieservices.commons.db.operations.IDataSource;
import org.ekstep.genieservices.commons.db.operations.impl.SQLiteDataSource;
import org.ekstep.genieservices.commons.utils.FileUtil;

import java.util.List;
import java.util.Map;

/**
 * Created on 6/8/2017.
 *
 * @author anil
 */
public class FileImporter {

    private static final String TAG = FileImporter.class.getSimpleName();

    private IUserService userService;
    private IDataSource dataSource;

    public FileImporter(AppContext<Context> appContext, IUserService userService) {
        this.userService = userService;
        dataSource = new SQLiteDataSource(appContext);
    }

    public GenieResponse<Void> importFile(ProfileImportRequest importRequest) {
        GenieResponse<Void> response;
        if (!FileUtil.doesFileExists(importRequest.getSourceFilePath())) {
            response = GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.INVALID_FILE, "Import failed, file doesn't exists", TAG);
            return response;
        }

        String ext = FileUtil.getFileExtension(importRequest.getSourceFilePath());
        if (!ServiceConstants.FileExtension.PROFILE.equals(ext)) {
            response = GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.INVALID_FILE, "Profile import failed, unsupported file extension", TAG);
            return response;
        } else {
            IDBSession dbSession = dataSource.getImportDataSource(importRequest.getSourceFilePath());
            MetadataModel metadataModel = MetadataModel.findAll(dbSession);
            if (metadataModel != null) {
                Map<String, Object> metadata = metadataModel.getMetadata();
                List<String> importTypes = getImportTypes(metadata);
                if (importTypes != null && importTypes.contains(ServiceConstants.EXPORT_TYPE_PROFILE)) {
                    return userService.importProfile(dbSession, metadata);
                }
            }

            response = GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.INVALID_FILE, "Profile import failed, metadata.", TAG);
            return response;
        }
    }

    private List<String> getImportTypes(Map<String, Object> metadata) {
        return (List<String>) metadata.get(ServiceConstants.EXPORT_TYPES);
    }

}
