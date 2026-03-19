package com.urlshortener.service;

import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.ShortenResponse;
import com.urlshortener.exception.CodeAlreadyExistsException;
import com.urlshortener.exception.UrlNotFoundException;
import com.urlshortener.model.ClickEvent;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.util.Base62Encoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
public class UrlShortenerService {

    private static final Logger log = LoggerFactory.getLogger(UrlShortenerService.class);

    private static final String CACHE_KEY_PREFIX = "url:";
    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final UrlRepository urlRepository;
    private final AnalyticsService analyticsService;
    private final Base62Encoder base62Encoder;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.cache.url-ttl-minutes:60}")
    private long cacheTtlMinutes;

    @Value("${app.dynamodb.default-ttl-days:30}")
    private int defaultTtlDays;

    public UrlShortenerService(UrlRepository urlRepository, AnalyticsService analyticsService, Base62Encoder base62Encoder, RedisTemplate<String, Object> redisTemplate) {
        this.urlRepository = urlRepository;
        this.analyticsService = analyticsService;
        this.base62Encoder = base62Encoder;
        this.redisTemplate = redisTemplate;
    }

    // ─── Shorten ──────────────────────────────────────────────────────────────

    public ShortenResponse shorten(ShortenRequest request) {
        String originalUrl = request.getOriginalUrl();
        String code;

        if (request.getCustomCode() != null && !request.getCustomCode().isBlank()) {
            code = request.getCustomCode();
            // Check for collision
            urlRepository.findByCode(code).ifPresent(existing -> {
                throw new CodeAlreadyExistsException(code);
            });
        } else {
            code = generateUniqueCode(originalUrl);
        }

        int ttlDays = (request.getTtlDays() != null && request.getTtlDays() > 0)
                ? request.getTtlDays()
                : defaultTtlDays;

        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofDays(ttlDays));
        String shortUrl = baseUrl + "/" + code;

        UrlMapping mapping = UrlMapping.builder()
                .code(code)
                .originalUrl(originalUrl)
                .shortUrl(shortUrl)
                .createdAt(ISO_FMT.format(now.atZone(ZoneOffset.UTC)))
                .expiresAt(expiresAt.getEpochSecond())
                .clickCount(0L)
                .customAlias(request.getCustomCode())
                .build();

        urlRepository.save(mapping);
        cacheMapping(code, mapping);

        log.info("Shortened URL: {} -> {} (expires: {})", originalUrl, shortUrl, expiresAt);

        return toResponse(mapping, expiresAt);
    }

    // ─── Resolve (redirect) ───────────────────────────────────────────────────

    public String resolve(String code, String userAgent, String ip, String referer) {
        UrlMapping mapping = getCachedOrFetch(code);
        String originalUrl = mapping.getOriginalUrl();

        // Publish click event asynchronously (non-blocking)
        ClickEvent event = ClickEvent.builder()
                .code(code)
                .originalUrl(originalUrl)
                .userAgent(userAgent)
                .ipAddress(ip)
                .referer(referer)
                .clickedAt(Instant.now())
                .build();
        analyticsService.publishClickEvent(event);
        urlRepository.incrementClickCount(code);

        return originalUrl;
    }

    // ─── Get Info ─────────────────────────────────────────────────────────────

    public ShortenResponse getInfo(String code) {
        UrlMapping mapping = getCachedOrFetch(code);
        Instant expiresAt = mapping.getExpiresAt() != null
                ? Instant.ofEpochSecond(mapping.getExpiresAt())
                : null;
        return toResponse(mapping, expiresAt);
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    public void delete(String code) {
        urlRepository.findByCode(code)
                .orElseThrow(() -> new UrlNotFoundException(code));
        urlRepository.delete(code);
        evictCache(code);
        log.info("Deleted short URL: {}", code);
    }

    // ─── Internal helpers ─────────────────────────────────────────────────────

    private String generateUniqueCode(String originalUrl) {
        try {
            String input = originalUrl + Instant.now().toEpochMilli();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Convert first 8 bytes of SHA-256 hash to a positive long
            long hashLong = 0;
            for (int i = 0; i < 8; i++) {
                hashLong = (hashLong << 8) | (hash[i] & 0xFF);
            }
            hashLong = Math.abs(hashLong);

            return base62Encoder.encode(hashLong);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private UrlMapping getCachedOrFetch(String code) {
        String cacheKey = CACHE_KEY_PREFIX + code;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof UrlMapping mapping) {
            log.debug("Cache HIT for code={}", code);
            return mapping;
        }

        log.debug("Cache MISS for code={}, fetching from DynamoDB", code);
        UrlMapping mapping = urlRepository.findByCode(code)
                .orElseThrow(() -> new UrlNotFoundException(code));

        cacheMapping(code, mapping);
        return mapping;
    }

    private void cacheMapping(String code, UrlMapping mapping) {
        String cacheKey = CACHE_KEY_PREFIX + code;
        redisTemplate.opsForValue().set(cacheKey, mapping, Duration.ofMinutes(cacheTtlMinutes));
    }

    private void evictCache(String code) {
        redisTemplate.delete(CACHE_KEY_PREFIX + code);
    }

    private ShortenResponse toResponse(UrlMapping mapping, Instant expiresAt) {
        return ShortenResponse.builder()
                .code(mapping.getCode())
                .shortUrl(mapping.getShortUrl())
                .originalUrl(mapping.getOriginalUrl())
                .createdAt(mapping.getCreatedAt())
                .expiresAt(expiresAt != null ? ISO_FMT.format(expiresAt.atZone(ZoneOffset.UTC)) : null)
                .clickCount(mapping.getClickCount())
                .build();
    }
}
