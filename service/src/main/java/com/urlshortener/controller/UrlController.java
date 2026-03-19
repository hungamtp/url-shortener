package com.urlshortener.controller;

import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.ShortenResponse;
import com.urlshortener.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "URL Shortener", description = "URL shortening and redirect operations")
public class UrlController {

    private static final Logger log = LoggerFactory.getLogger(UrlController.class);

    private final UrlShortenerService urlShortenerService;

    public UrlController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    // ─── POST /api/v1/urls — Shorten a URL ──────────────────────────────────

    @Operation(summary = "Shorten a URL",
               description = "Creates a short URL from a long URL. Optionally accepts a custom code and TTL.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Short URL created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request (bad URL or validation failure)"),
            @ApiResponse(responseCode = "409", description = "Custom code already exists")
    })
    @PostMapping("/api/v1/urls")
    public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest request) {
        log.info("Shorten request: {}", request.getOriginalUrl());
        ShortenResponse response = urlShortenerService.shorten(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─── GET /{code} — Redirect ──────────────────────────────────────────────

    @Operation(summary = "Redirect to original URL",
               description = "Resolves a short code and redirects (HTTP 302) to the original URL. Records a click event.")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Redirect to original URL"),
            @ApiResponse(responseCode = "404", description = "Short code not found")
    })
    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(
            @Parameter(description = "Short URL code") @PathVariable String code,
            HttpServletRequest request) {

        String userAgent = request.getHeader("User-Agent");
        String ip        = request.getRemoteAddr();
        String referer   = request.getHeader("Referer");

        String originalUrl = urlShortenerService.resolve(code, userAgent, ip, referer);
        log.info("Redirecting code={} to {}", code, originalUrl);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, originalUrl)
                .build();
    }

    // ─── GET /api/v1/urls/{code} — Get Info ─────────────────────────────────

    @Operation(summary = "Get URL info",
               description = "Returns metadata for a short URL including click count and expiry.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "URL info returned"),
            @ApiResponse(responseCode = "404", description = "Short code not found")
    })
    @GetMapping("/api/v1/urls/{code}")
    public ResponseEntity<ShortenResponse> getInfo(
            @Parameter(description = "Short URL code") @PathVariable String code) {
        return ResponseEntity.ok(urlShortenerService.getInfo(code));
    }

    // ─── DELETE /api/v1/urls/{code} — Delete ────────────────────────────────

    @Operation(summary = "Delete a short URL",
               description = "Permanently deletes a short URL from the database and cache.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Short code not found")
    })
    @DeleteMapping("/api/v1/urls/{code}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Short URL code") @PathVariable String code) {
        urlShortenerService.delete(code);
        return ResponseEntity.noContent().build();
    }
}
