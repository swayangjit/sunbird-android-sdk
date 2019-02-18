package org.ekstep.genieservices.commons.bean;

import org.ekstep.genieservices.commons.utils.StringUtil;

public class VerifyOTPRequest {

    private String key;
    private String type;
    private String otp;

    private VerifyOTPRequest(String key, String type, String otp) {
        this.key = key;
        this.type = type;
        this.otp = otp;
    }

    public String getKey() {
        return key;
    }

    public String getType() {
        return type;
    }

    public String getOtp() {
        return otp;
    }

    public static class Builder {
        private String key;
        private String type;
        private String otp;

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

        public Builder otp(String otp) {
            if (StringUtil.isNullOrEmpty(otp)) {
                throw new IllegalArgumentException("otp should not be null or empty.");
            }
            this.otp = otp;
            return this;
        }

        public VerifyOTPRequest build() {
            if (StringUtil.isNullOrEmpty(key)) {
                throw new IllegalStateException("key required.");
            }

            if (StringUtil.isNullOrEmpty(type)) {
                throw new IllegalStateException("type required.");
            }

            if (StringUtil.isNullOrEmpty(otp)) {
                throw new IllegalStateException("otp required.");
            }

            return new VerifyOTPRequest(key, type, otp);
        }
    }
}
