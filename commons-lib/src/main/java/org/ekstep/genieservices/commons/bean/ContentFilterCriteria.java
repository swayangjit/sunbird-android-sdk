package org.ekstep.genieservices.commons.bean;

import org.ekstep.genieservices.commons.utils.GsonUtil;

/**
 * This class holds the uid, types of content required and attachFeedback, attachContentAccess flags if the feedback and content access are required.
 */
public class ContentFilterCriteria {

    private String uid;
    private String[] contentTypes;
    private String[] audience;
    private boolean attachFeedback;
    private boolean attachContentAccess;

    private ContentFilterCriteria(String uid, String[] contentTypes, String[] audience, boolean attachFeedback, boolean attachContentAccess) {
        this.uid = uid;
        this.contentTypes = contentTypes;
        this.audience = audience;
        this.attachFeedback = attachFeedback;
        this.attachContentAccess = attachContentAccess;
    }

    public String getUid() {
        return uid;
    }

    public String[] getContentTypes() {
        return contentTypes;
    }

    public String[] getAudience() {
        return audience;
    }

    public boolean attachFeedback() {
        return attachFeedback;
    }

    public boolean attachContentAccess() {
        return attachContentAccess;
    }

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }

    public static class Builder {
        private String uid;
        private String[] contentTypes;
        private String[] audience;
        private boolean attachFeedback;
        private boolean attachContentAccess;

        /**
         * User id to get the content in order to access by that user.
         * And also required when want feedback and content access.
         */
        public Builder forUser(String uid) {
            this.uid = uid;
            return this;
        }

        public Builder contentTypes(String[] contentTypes) {
            this.contentTypes = contentTypes;
            return this;
        }

        /**
         * Pass true if want feedback, provided by given uid else false.
         */
        public Builder withFeedback() {
            this.attachFeedback = true;
            return this;
        }

        public Builder audience(String[] audience) {
            this.audience = audience;
            return this;
        }

        /**
         * Pass true if want content access by given uid else false.
         */
        public Builder withContentAccess() {
            this.attachContentAccess = true;
            return this;
        }

        public ContentFilterCriteria build() {
            if (contentTypes == null || contentTypes.length == 0) {
                contentTypes = new String[]{"Story", "Worksheet", "Collection", "Game", "TextBook"};
            }
            return new ContentFilterCriteria(uid, contentTypes, audience, attachFeedback, attachContentAccess);
        }
    }
}
