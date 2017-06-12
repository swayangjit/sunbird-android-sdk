package org.ekstep.genieservices.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import org.ekstep.genieservices.Constants;
import org.ekstep.genieservices.commons.bean.Content;
import org.ekstep.genieservices.commons.bean.ContentData;
import org.ekstep.genieservices.commons.bean.ContentHierarchy;
import org.ekstep.genieservices.commons.bean.enums.ContentType;
import org.ekstep.genieservices.commons.utils.GsonUtil;
import org.ekstep.genieservices.commons.utils.Logger;
import org.ekstep.genieservices.commons.utils.ReflectionUtil;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by swayangjit on 1/6/17.
 */

public class ContentPlayer {

    private static final String TAG = ContentPlayer.class.getSimpleName();
    private static final String GENIE_CANVAS_PACKAGE = "org.ekstep.geniecanvas";
    private static final String GENIE_QUIZ_APP_PACKAGE = "org.ekstep.quiz.app";
    private static final String GENIE_CANVAS_ACTIVITY = "org.ekstep.geniecanvas.MainActivity";
    private static ContentPlayer sContentPlayer;
    private String mQualifier;

    private ContentPlayer(String qualifier) {
        this.mQualifier = qualifier;
    }

    public static void init(String qualifier) {
        if (sContentPlayer == null) {
            sContentPlayer = new ContentPlayer(qualifier);
        }
    }

    public static void play(Context context, Content content, Map<String, Object> resourceBundle) {
        PackageManager manager = context.getPackageManager();
        ContentData contentData = content.getContentData();
        String osId = contentData.getOsId();
        if (sContentPlayer.mQualifier == null) {
            Toast.makeText(context, "App qualifier not found", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = null;
        if (content.getMimeType().equalsIgnoreCase(Constants.MimeType.APK_MIME_TYPE)) {
            if (ContentUtil.isAppInstalled(context, osId)) {
                intent = manager.getLaunchIntentForPackage(osId);
            } else {
                ContentUtil.openPlaystore(context, osId);
                return;
            }

        } else if (content.getMimeType().equalsIgnoreCase(Constants.MimeType.ECML_MIME_TYPE) && !isCollectionorTextBook(content.getContentType())) {
            if (osId == null || GENIE_QUIZ_APP_PACKAGE.equals(osId) || GENIE_CANVAS_PACKAGE.equals(osId)) {
                Class<?> className = ReflectionUtil.getClass(GENIE_CANVAS_ACTIVITY);
                if (className == null) {
                    Toast.makeText(context, "Content player not found", Toast.LENGTH_SHORT).show();
                    return;
                }
                intent = new Intent(context, className);
            }
        } else {
            Toast.makeText(context, "Content type not supported", Toast.LENGTH_SHORT).show();
            return;
        }

        intent.putExtra("origin", "Genie");
//        if (hierarchyList != null && hierarchyList.size() > 0) {
//            intent.putExtra("contentExtras", getContentHierarchy(hierarchyList, contentData.getIdentifier(), content.getContentType()));
//        }
        // TODO: 9/6/17 This will be updated when content child will be done
        intent.putExtra("contentExtras", new ArrayList<>());
        intent.putExtra("appInfo", GsonUtil.toJson(contentData));
        intent.putExtra("languageInfo", GsonUtil.toJson(resourceBundle));
        intent.putExtra("appQualifier", sContentPlayer.mQualifier);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        context.startActivity(intent);

    }

    //Added this method not to mess up with the main list.The issue was if we play the content again and again then Content info was being added repeatedly.
    private static String getContentHierarchy(ArrayList<ContentHierarchy> contentInfoList, String identifier, String contentType) {
        ArrayList<ContentHierarchy> infoList = new ArrayList<>();
        infoList.addAll(contentInfoList);
        infoList.add(new ContentHierarchy(identifier, contentType));
        Logger.i("ContentHierarchyList", "" + infoList);
        return GsonUtil.toJson(infoList);
    }

    private static boolean isCollectionorTextBook(String contentType) {
        return contentType.equalsIgnoreCase(ContentType.COLLECTION.getValue())
                || contentType.equalsIgnoreCase(ContentType.TEXTBOOK.getValue())
                || contentType.equalsIgnoreCase(ContentType.TEXTBOOK_UNIT.getValue());
    }
}