package org.ekstep.genieservices.commons.bean;

import org.ekstep.genieservices.commons.utils.StringUtil;

/**
 * Created on 13/3/18.
 *
 * @author anil
 */
public class FrameworkDetailsRequest {

    private String frameworkId;
    private String[] categories;
    private boolean refreshFrameworkDetails;
    private String filePath;

    private FrameworkDetailsRequest(String frameworkId, String[] categories, boolean refreshFrameworkDetails, String filePath) {
        this.frameworkId = frameworkId;
        this.categories = categories;
        this.refreshFrameworkDetails = refreshFrameworkDetails;
        this.filePath = filePath;
    }

    public String getFrameworkId() {
        return frameworkId;
    }

    public String[] getCategories() {
        return categories;
    }

    public boolean isRefreshFrameworkDetails() {
        return refreshFrameworkDetails;
    }

    public String getFilePath() {
        return filePath;
    }

    public static class Builder {

        private String frameworkId;
        private String[] categories;
        private boolean refreshFrameworkDetails;
        private String filePath;

        public Builder forFramework(String frameworkId) {
            if (StringUtil.isNullOrEmpty(frameworkId)) {
                throw new IllegalArgumentException("frameworkId should not be null or empty.");
            }
            this.frameworkId = frameworkId;
            return this;
        }

        /**
         * Array of category. i.e. "board", "gradeLevel", "subject", "medium", "topic", "purpose"
         */
        public Builder frameworkCategories(String[] categories) {
            this.categories = categories;
            return this;
        }

        /**
         * The framework details are refreshed from the server only if this flag is set.
         */
        public Builder refreshFrameworkDetailsFromServer() {
            this.refreshFrameworkDetails = true;
            return this;
        }

        public Builder fromFilePath(String filePath) {
            if (StringUtil.isNullOrEmpty(filePath)) {
                throw new IllegalArgumentException("filePath should not be null or empty.");
            }

            this.filePath = filePath;
            return this;
        }

        public FrameworkDetailsRequest build() {
            if (StringUtil.isNullOrEmpty(frameworkId)) {
                throw new IllegalStateException("frameworkId required.");
            }

            if (categories == null || categories.length == 0) {
                this.categories = new String[]{"board", "medium", "gradeLevel", "subject"};
            }

            return new FrameworkDetailsRequest(frameworkId, categories, refreshFrameworkDetails, filePath);
        }
    }
}
