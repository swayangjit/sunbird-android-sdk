package org.ekstep.genieservices.contentfeedback;

import android.os.Environment;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import junit.framework.Assert;

import org.ekstep.genieservices.GenieServiceDBHelper;
import org.ekstep.genieservices.GenieServiceTestBase;
import org.ekstep.genieservices.commons.bean.Content;
import org.ekstep.genieservices.commons.bean.ContentDetailsRequest;
import org.ekstep.genieservices.commons.bean.ContentFeedback;
import org.ekstep.genieservices.commons.bean.ContentFeedbackFilterCriteria;
import org.ekstep.genieservices.commons.bean.EcarImportRequest;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.bean.Profile;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Created by Sneha on 6/2/2017.
 */
@RunWith(AndroidJUnit4.class)
public class ContentFeedbackServiceTest extends GenieServiceTestBase {
    private static final String TAG = ContentFeedbackServiceTest.class.getSimpleName();
    final String CONTENT_ID = "do_30013486";
    private final String CONTENT_FILEPATH = Environment.getExternalStorageDirectory().toString() + "/Download/Multiplication2.ecar";

    @Test
    public void shouldSendFeedbackForEcar() {

        GenieServiceDBHelper.clearEcarEntryFromDB();

        //create profile
        String uid = createAndSetProfileForGetFeedback();

        //import content
        importContent();

        //send feedback
        ContentFeedback sendFeedbackData = new ContentFeedback();
        sendFeedbackData.setComments("Great content");
        sendFeedbackData.setContentId(CONTENT_ID);
        sendFeedbackData.setRating(3);
        GenieResponse genieResponseSendFeedback = activity.sendFeedback(sendFeedbackData);
        Assert.assertTrue(genieResponseSendFeedback.getStatus());

        //assert send feedback data
        shouldAssertSendFeedbackData(sendFeedbackData);

        //get feedback
        ContentFeedbackFilterCriteria.Builder data = new ContentFeedbackFilterCriteria.Builder()
                .byUser(uid).forContent(CONTENT_ID);
        GenieResponse<List<ContentFeedback>> genieResponseGetFeedback = activity.getFeedback(data.build());
        Assert.assertTrue(genieResponseGetFeedback.getStatus());
        ContentFeedback getFeedbackData = genieResponseGetFeedback.getResult().get(0);

        //assert get feedback data
        shouldAssertGetFeedbackData(sendFeedbackData, getFeedbackData);
    }

    private void shouldAssertGetFeedbackData(ContentFeedback contentFeedback, ContentFeedback getFeedback) {
        Assert.assertEquals(contentFeedback.getContentId(), getFeedback.getContentId());
        Assert.assertEquals(contentFeedback.getRating(), getFeedback.getRating());
        Assert.assertEquals(contentFeedback.getComments(), getFeedback.getComments());
    }

    /**
     * Should assert the feedback data got in the content details API.
     *
     * @param contentFeedback
     */
    private void shouldAssertSendFeedbackData(ContentFeedback contentFeedback) {

        GenieServiceDBHelper.findContentEntryInDB(CONTENT_ID);

        ContentDetailsRequest.Builder detailsRequest = new ContentDetailsRequest.Builder().forContent(CONTENT_ID).withFeedback();
        GenieResponse<Content> genieResponseDetails = activity.getContentDetails(detailsRequest.build());

        Assert.assertTrue("true", genieResponseDetails.getStatus());
        Assert.assertEquals("worksheet", genieResponseDetails.getResult().getContentType());
        Assert.assertTrue("true", genieResponseDetails.getResult().isAvailableLocally());
        Assert.assertEquals(CONTENT_ID, genieResponseDetails.getResult().getIdentifier());
        Assert.assertNotNull(genieResponseDetails.getResult().getContentFeedback());

        Assert.assertEquals(contentFeedback.getComments(), genieResponseDetails.getResult().getContentFeedback().get(0).getComments());
        Assert.assertEquals(contentFeedback.getContentId(), genieResponseDetails.getResult().getContentFeedback().get(0).getContentId());
        Assert.assertEquals(contentFeedback.getRating(), genieResponseDetails.getResult().getContentFeedback().get(0).getRating());
        Assert.assertNotNull(genieResponseDetails.getResult().getContentFeedback().get(0).getCreatedAt());
    }

    private String createAndSetProfileForGetFeedback() {

        GenieServiceDBHelper.clearProfileTable();
        Profile profile = new Profile("Happy1", "@drawable/ic_avatar2", "en");
        profile.setAge(4);
        profile.setDay(12);
        profile.setMonth(11);

        Profile createdProfile = createNewProfile(profile);

        GenieResponse responseSetCurrentUser = activity.setCurrentUser(createdProfile.getUid());
        Assert.assertTrue(responseSetCurrentUser.getStatus());

        GenieResponse<Profile> responseGetCurrentUser = activity.getCurrentUser();
        Assert.assertTrue(responseGetCurrentUser.getStatus());
        String uid = responseGetCurrentUser.getResult().getUid();

        return uid;
    }

    private void importContent() {
        EcarImportRequest.Builder importRequest = new EcarImportRequest.Builder().fromFilePath(CONTENT_FILEPATH)
                .toFolder(activity.getExternalFilesDir(null).toString());
        GenieResponse genieResponse = activity.importEcar(importRequest.build());
        Assert.assertTrue(genieResponse.getStatus());
    }

}
