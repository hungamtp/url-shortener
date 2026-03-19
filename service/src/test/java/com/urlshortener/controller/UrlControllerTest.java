package com.urlshortener.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.ShortenResponse;
import com.urlshortener.exception.GlobalExceptionHandler;
import com.urlshortener.exception.UrlNotFoundException;
import com.urlshortener.service.UrlShortenerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UrlController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("UrlController Web MVC Tests")
class UrlControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  UrlShortenerService urlShortenerService;

    @Test
    @DisplayName("POST /api/v1/urls - valid request - should return 201")
    void shorten_validRequest_shouldReturn201() throws Exception {
        ShortenRequest request = new ShortenRequest("https://www.google.com", null, null);
        ShortenResponse response = ShortenResponse.builder()
                .code("abc1234")
                .shortUrl("http://localhost:8080/abc1234")
                .originalUrl("https://www.google.com")
                .clickCount(0L)
                .build();
        when(urlShortenerService.shorten(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("abc1234"))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/abc1234"));
    }

    @Test
    @DisplayName("POST /api/v1/urls - blank URL - should return 400")
    void shorten_blankUrl_shouldReturn400() throws Exception {
        ShortenRequest request = new ShortenRequest("", null, null);

        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /{code} - valid code - should redirect 302")
    void redirect_validCode_shouldReturn302() throws Exception {
        when(urlShortenerService.resolve(any(), any(), any(), any()))
                .thenReturn("https://www.google.com");

        mockMvc.perform(get("/abc1234"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://www.google.com"));
    }

    @Test
    @DisplayName("GET /{code} - not found - should return 404")
    void redirect_notFound_shouldReturn404() throws Exception {
        when(urlShortenerService.resolve(any(), any(), any(), any()))
                .thenThrow(new UrlNotFoundException("missing"));

        mockMvc.perform(get("/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/v1/urls/{code} - valid code - should return 200 with info")
    void getInfo_validCode_shouldReturn200() throws Exception {
        ShortenResponse response = ShortenResponse.builder()
                .code("abc1234")
                .shortUrl("http://localhost:8080/abc1234")
                .originalUrl("https://www.google.com")
                .clickCount(5L)
                .build();
        when(urlShortenerService.getInfo("abc1234")).thenReturn(response);

        mockMvc.perform(get("/api/v1/urls/abc1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clickCount").value(5));
    }

    @Test
    @DisplayName("DELETE /api/v1/urls/{code} - valid code - should return 204")
    void delete_validCode_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/urls/abc1234"))
                .andExpect(status().isNoContent());
    }
}
