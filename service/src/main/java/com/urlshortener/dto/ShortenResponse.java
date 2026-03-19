package com.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShortenResponse {

    private String code;
    private String shortUrl;
    private String originalUrl;
    private String createdAt;
    private String expiresAt;
    private Long clickCount;

    public ShortenResponse() {}

    public ShortenResponse(String code, String shortUrl, String originalUrl, String createdAt, String expiresAt, Long clickCount) {
        this.code = code;
        this.shortUrl = shortUrl;
        this.originalUrl = originalUrl;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.clickCount = clickCount;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getShortUrl() { return shortUrl; }
    public void setShortUrl(String shortUrl) { this.shortUrl = shortUrl; }

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    public Long getClickCount() { return clickCount; }
    public void setClickCount(Long clickCount) { this.clickCount = clickCount; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String code;
        private String shortUrl;
        private String originalUrl;
        private String createdAt;
        private String expiresAt;
        private Long clickCount;

        public Builder code(String code) { this.code = code; return this; }
        public Builder shortUrl(String shortUrl) { this.shortUrl = shortUrl; return this; }
        public Builder originalUrl(String originalUrl) { this.originalUrl = originalUrl; return this; }
        public Builder createdAt(String createdAt) { this.createdAt = createdAt; return this; }
        public Builder expiresAt(String expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder clickCount(Long clickCount) { this.clickCount = clickCount; return this; }

        public ShortenResponse build() {
            return new ShortenResponse(code, shortUrl, originalUrl, createdAt, expiresAt, clickCount);
        }
    }
}
