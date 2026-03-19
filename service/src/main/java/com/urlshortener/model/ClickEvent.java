package com.urlshortener.model;

import java.time.Instant;

public class ClickEvent {

    private String code;
    private String originalUrl;
    private String userAgent;
    private String ipAddress;
    private String referer;
    private Instant clickedAt;

    public ClickEvent() {}

    public ClickEvent(String code, String originalUrl, String userAgent, String ipAddress, String referer, Instant clickedAt) {
        this.code = code;
        this.originalUrl = originalUrl;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
        this.referer = referer;
        this.clickedAt = clickedAt;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getReferer() { return referer; }
    public void setReferer(String referer) { this.referer = referer; }

    public Instant getClickedAt() { return clickedAt; }
    public void setClickedAt(Instant clickedAt) { this.clickedAt = clickedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String code;
        private String originalUrl;
        private String userAgent;
        private String ipAddress;
        private String referer;
        private Instant clickedAt;

        public Builder code(String code) { this.code = code; return this; }
        public Builder originalUrl(String originalUrl) { this.originalUrl = originalUrl; return this; }
        public Builder userAgent(String userAgent) { this.userAgent = userAgent; return this; }
        public Builder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public Builder referer(String referer) { this.referer = referer; return this; }
        public Builder clickedAt(Instant clickedAt) { this.clickedAt = clickedAt; return this; }

        public ClickEvent build() {
            return new ClickEvent(code, originalUrl, userAgent, ipAddress, referer, clickedAt);
        }
    }
}
