package org.ekstep.genieservices.commons.bean;

/**
 * Created on 6/6/2017.
 *
 * @author anil
 */
public class ContentFeedbackFilterCriteria {

    private String uid;
    private String contentId;

    private ContentFeedbackFilterCriteria(String uid, String contentId) {
        this.uid = uid;
        this.contentId = contentId;
    }

    public String getUid() {
        return uid;
    }

    public String getContentId() {
        return contentId;
    }

    public static class Builder {
        private String uid;
        private String contentId;

        public Builder byUser(String uid) {
            this.uid = uid;
            return this;
        }

        public Builder forContent(String contentId) {
            this.contentId = contentId;
            return this;
        }

        public ContentFeedbackFilterCriteria build() {
            return new ContentFeedbackFilterCriteria(uid, contentId);
        }
    }
}