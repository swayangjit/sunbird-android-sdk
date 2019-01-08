package org.ekstep.genieservices.commons.bean;

import org.ekstep.genieservices.commons.utils.StringUtil;

public class LocationSearchCriteria {

    private String query;
    private String type;
    private String parentId;
    private String code;
    private int limit;
    private int offset;

    private LocationSearchCriteria(String query, String type, String parentId, String code, int offset, int limit) {
        this.query = query;
        this.type = type;
        this.parentId = parentId;
        this.code = code;
        this.offset = offset;
        this.limit = limit;
    }

    public String getQuery() {
        return query;
    }

    public String getType() {
        return type;
    }

    public String getParentId() {
        return parentId;
    }

    public String getCode() {
        return code;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public static class SearchBuilder {

        private String query;
        private String type;
        private String parentId;
        private String code;
        private int limit;
        private int offset;

        public SearchBuilder searchQuery(String query) {
            if (StringUtil.isNullOrEmpty(query)) {
                throw new IllegalArgumentException("query cannot be empty");
            }

            this.query = query;
            return this;
        }

        public SearchBuilder type(String type) {
            if (StringUtil.isNullOrEmpty(type)) {
                throw new IllegalArgumentException("type cannot be null or empty");
            }

            this.type = type;
            return this;
        }

        public SearchBuilder parentId(String parentId) {
            if (StringUtil.isNullOrEmpty(parentId)) {
                throw new IllegalArgumentException("parentId cannot be null or empty");
            }

            this.parentId = parentId;
            return this;
        }

        public SearchBuilder code(String code) {
            if (StringUtil.isNullOrEmpty(code)) {
                throw new IllegalArgumentException("code cannot be null or empty");
            }

            this.code = code;
            return this;
        }

        public SearchBuilder setOffset(int offset) {
            if (offset < 0) {
                throw new IllegalArgumentException("offset should be greater than 0");
            }

            this.offset = offset;
            return this;
        }

        public SearchBuilder limit(int limit) {
            if (limit < 0) {
                throw new IllegalArgumentException("limit should be greater than 0");
            }

            this.limit = limit;
            return this;
        }

        public LocationSearchCriteria build() {
            return new LocationSearchCriteria(query, type, parentId, code, offset, limit);
        }
    }

}
