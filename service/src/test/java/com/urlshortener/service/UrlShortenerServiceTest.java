package com.urlshortener.service;

import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.ShortenResponse;
import com.urlshortener.exception.CodeAlreadyExistsException;
import com.urlshortener.exception.UrlNotFoundException;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.util.Base62Encoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UrlShortenerService Unit Tests")
class UrlShortenerServiceTest {

    @Mock private UrlRepository urlRepository;
    @Mock private AnalyticsService analyticsService;
    @Mock private Base62Encoder base62Encoder;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOps;

    @InjectMocks
    private UrlShortenerService urlShortenerService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(urlShortenerService, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(urlShortenerService, "cacheTtlMinutes", 60L);
        ReflectionTestUtils.setField(urlShortenerService, "defaultTtlDays", 30);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    @DisplayName("shorten() - auto-generated code - should save and return response")
    void shorten_autoCode_shouldSaveAndReturn() {
        // Given
        ShortenRequest request = new ShortenRequest("https://www.google.com", null, null);
        when(base62Encoder.encode(anyLong())).thenReturn("abc1234");

        // When
        ShortenResponse response = urlShortenerService.shorten(request);

        // Then
        assertThat(response.getOriginalUrl()).isEqualTo("https://www.google.com");
        assertThat(response.getShortUrl()).isEqualTo("http://localhost:8080/abc1234");
        assertThat(response.getCode()).isEqualTo("abc1234");
        verify(urlRepository).save(any(UrlMapping.class));
    }

    @Test
    @DisplayName("shorten() - custom code - should use provided code")
    void shorten_customCode_shouldUseProvidedCode() {
        // Given
        ShortenRequest request = new ShortenRequest("https://example.com", "my-link", 7);
        when(urlRepository.findByCode("my-link")).thenReturn(Optional.empty());

        // When
        ShortenResponse response = urlShortenerService.shorten(request);

        // Then
        assertThat(response.getCode()).isEqualTo("my-link");
        assertThat(response.getShortUrl()).isEqualTo("http://localhost:8080/my-link");
        verify(urlRepository).save(argThat(m -> "my-link".equals(m.getCode())));
    }

    @Test
    @DisplayName("shorten() - custom code collision - should throw CodeAlreadyExistsException")
    void shorten_customCodeExists_shouldThrow() {
        // Given
        ShortenRequest request = new ShortenRequest("https://example.com", "taken", null);
        UrlMapping existing = UrlMapping.builder().code("taken").build();
        when(urlRepository.findByCode("taken")).thenReturn(Optional.of(existing));

        // Then
        assertThatThrownBy(() -> urlShortenerService.shorten(request))
                .isInstanceOf(CodeAlreadyExistsException.class)
                .hasMessageContaining("taken");
    }

    @Test
    @DisplayName("resolve() - cache miss - should fetch from DynamoDB and publish click event")
    void resolve_cacheMiss_shouldFetchAndPublish() {
        // Given
        UrlMapping mapping = UrlMapping.builder()
                .code("abc1234")
                .originalUrl("https://www.google.com")
                .clickCount(0L)
                .expiresAt(9999999999L)
                .build();
        when(valueOps.get("url:abc1234")).thenReturn(null);
        when(urlRepository.findByCode("abc1234")).thenReturn(Optional.of(mapping));

        // When
        String result = urlShortenerService.resolve("abc1234", "Mozilla", "127.0.0.1", null);

        // Then
        assertThat(result).isEqualTo("https://www.google.com");
        verify(analyticsService).publishClickEvent(any());
        verify(urlRepository).incrementClickCount("abc1234");
    }

    @Test
    @DisplayName("resolve() - not found - should throw UrlNotFoundException")
    void resolve_notFound_shouldThrow() {
        // Given
        when(valueOps.get("url:missing")).thenReturn(null);
        when(urlRepository.findByCode("missing")).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> urlShortenerService.resolve("missing", null, null, null))
                .isInstanceOf(UrlNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    @DisplayName("delete() - existing code - should remove from DB and evict cache")
    void delete_existing_shouldRemoveAndEvict() {
        // Given
        UrlMapping mapping = UrlMapping.builder().code("abc1234").build();
        when(urlRepository.findByCode("abc1234")).thenReturn(Optional.of(mapping));

        // When
        urlShortenerService.delete("abc1234");

        // Then
        verify(urlRepository).delete("abc1234");
        verify(redisTemplate).delete("url:abc1234");
    }

    @Test
    @DisplayName("delete() - not found - should throw UrlNotFoundException")
    void delete_notFound_shouldThrow() {
        // Given
        when(urlRepository.findByCode("none")).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> urlShortenerService.delete("none"))
                .isInstanceOf(UrlNotFoundException.class);
    }
}
