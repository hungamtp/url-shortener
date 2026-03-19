package com.urlshortener.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class UrlMapping {

    private String code;
    private String originalUrl;
    private String shortUrl;
    private String createdAt;
    private Long expiresAt;
    private Long clickCount;
    private String customAlias;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("code")
    public String getCode() {
        return code;
    }

    @DynamoDbAttribute("originalUrl")
    public String getOriginalUrl() {
        return originalUrl;
    }

    @DynamoDbAttribute("shortUrl")
    public String getShortUrl() {
        return shortUrl;
    }

    @DynamoDbAttribute("createdAt")
    public String getCreatedAt() {
        return createdAt;
    }

    @DynamoDbAttribute("expiresAt")
    public Long getExpiresAt() {
        return expiresAt;
    }

    @DynamoDbAttribute("clickCount")
    public Long getClickCount() {
        return clickCount;
    }

    @DynamoDbAttribute("customAlias")
    public String getCustomAlias() {
        return customAlias;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setClickCount(Long clickCount) {
        this.clickCount = clickCount;
    }

    public void setCustomAlias(String customAlias) {
        this.customAlias = customAlias;
    }

    public UrlMapping() {
    }

    public UrlMapping(String code, String originalUrl, String shortUrl, String createdAt, Long expiresAt, Long clickCount, String customAlias) {
        this.code = code;
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.clickCount = clickCount;
        this.customAlias = customAlias;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String code;
        private String originalUrl;
        private String shortUrl;
        private String createdAt;
        private Long expiresAt;
        private Long clickCount;
        private String customAlias;

        public Builder code(String code) { this.code = code; return this; }
        public Builder originalUrl(String originalUrl) { this.originalUrl = originalUrl; return this; }
        public Builder shortUrl(String shortUrl) { this.shortUrl = shortUrl; return this; }
        public Builder createdAt(String createdAt) { this.createdAt = createdAt; return this; }
        public Builder expiresAt(Long expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder clickCount(Long clickCount) { this.clickCount = clickCount; return this; }
        public Builder customAlias(String customAlias) { this.customAlias = customAlias; return this; }

        public UrlMapping build() {
            return new UrlMapping(code, originalUrl, shortUrl, createdAt, expiresAt, clickCount, customAlias);
        }
    }
}
