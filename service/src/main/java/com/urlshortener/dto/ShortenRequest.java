package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public class ShortenRequest {

    @NotBlank(message = "originalUrl must not be blank")
    @URL(message = "originalUrl must be a valid URL")
    private String originalUrl;

    /**
     * Optional custom alias for the short code (e.g. "my-link").
     * Must be 3-20 alphanumeric characters or hyphens.
     */
    @Size(min = 3, max = 20, message = "customCode must be between 3 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9-_]*$", message = "customCode must be alphanumeric (hyphens and underscores allowed)")
    private String customCode;

    /**
     * Number of days until the short URL expires.
     * If null or 0, the URL never expires.
     */
    @Positive(message = "ttlDays must be a positive number")
    private Integer ttlDays;

    public ShortenRequest() {
    }

    public ShortenRequest(String originalUrl, String customCode, Integer ttlDays) {
        this.originalUrl = originalUrl;
        this.customCode = customCode;
        this.ttlDays = ttlDays;
    }

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public String getCustomCode() { return customCode; }
    public void setCustomCode(String customCode) { this.customCode = customCode; }

    public Integer getTtlDays() { return ttlDays; }
    public void setTtlDays(Integer ttlDays) { this.ttlDays = ttlDays; }
}
