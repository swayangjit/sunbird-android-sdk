package org.ekstep.genieproviders.user;

import android.content.Context;

import org.ekstep.genieservices.GenieService;

import java.util.Arrays;
import java.util.List;

/**
 * Created on 22/5/17.
 * shriharsh
 */

public class ProfileUriHandlerFactory {
    public static List<CurrentUserUriHandler> uriHandlers(String AUTHORITY,
                                                          Context context,
                                                          String selection, String[] selectionArgs, GenieService genieService) {
        return Arrays.asList(
                new CurrentUserUriHandler(AUTHORITY, context, selection, selectionArgs, genieService)
        );
    }
}