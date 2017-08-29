package org.ekstep.genieservices.commons.bean;

/**
 * This class accepts uid, contentId and hierarchyInfo when requesting for learner summary details.
 *
 */
public class SummaryRequest {

    private String uid;
    private String contentId;
    private String hierarchyData;

    private SummaryRequest(String uid, String contentId, String hierarchyData) {
        this.uid = uid;
        this.contentId = contentId;
        this.hierarchyData = hierarchyData;
    }

    public String getUid() {
        return uid;
    }

    public String getContentId() {
        return contentId;
    }

    public String getHierarchyData() {
        return hierarchyData;
    }

    public static class Builder {
        private String uid;
        private String contentId;
        private String hierarchyData;

        public Builder() {
        }

        /**
         * Content id for which you want the summary.
         */
        public Builder contentId(String contentId) {
            this.contentId = contentId;
            return this;
        }

        /**
         * User id for whom you want the summary.
         */
        public Builder uid(String uid) {
            this.uid = uid;
            return this;
        }

        /**
         * Hierarchy data of the content of which you want the summary.
         */
        public Builder hierarchyData(String hierarchyData) {
            this.hierarchyData = hierarchyData;
            return this;
        }

        public SummaryRequest build() {
            return new SummaryRequest(uid, contentId, hierarchyData);
        }
    }

}
