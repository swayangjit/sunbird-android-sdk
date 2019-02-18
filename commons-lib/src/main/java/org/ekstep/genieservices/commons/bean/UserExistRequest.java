package org.ekstep.genieservices.commons.bean;

import org.ekstep.genieservices.commons.utils.StringUtil;

public class UserExistRequest {

    private String key;
    private String type;

    private UserExistRequest(String key, String type) {
        this.key = key;
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public String getType() {
        return type;
    }

    public static class Builder {
        private String key;
        private String type;

        public Builder key(String key) {
            if (StringUtil.isNullOrEmpty(key)) {
                throw new IllegalArgumentException("key should not be null or empty.");
            }
            this.key = key;
            return this;
        }

        public Builder type(String type) {
            if (StringUtil.isNullOrEmpty(type)) {
                throw new IllegalArgumentException("type should not be null or empty.");
            }
            this.type = type;
            return this;
        }

        public UserExistRequest build() {
            if (StringUtil.isNullOrEmpty(key)) {
                throw new IllegalStateException("key required.");
            }

            if (StringUtil.isNullOrEmpty(type)) {
                throw new IllegalStateException("type required.");
            }

            return new UserExistRequest(key, type);
        }
    }
}
