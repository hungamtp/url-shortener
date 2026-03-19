package com.urlshortener.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.model.ClickEvent;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final SqsTemplate sqsTemplate;
    private final ObjectMapper objectMapper;

    public AnalyticsService(SqsTemplate sqsTemplate, ObjectMapper objectMapper) {
        this.sqsTemplate = sqsTemplate;
        this.objectMapper = objectMapper;
    }

    @Value("${app.sqs.analytics-queue:url-analytics}")
    private String analyticsQueue;

    /**
     * Asynchronously publishes a click event to SQS.
     * This is called on every redirect to track analytics without blocking the response.
     */
    @Async
    public void publishClickEvent(ClickEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            sqsTemplate.send(analyticsQueue, payload);
            log.debug("Published click event for code={}", event.getCode());
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize click event for code={}: {}", event.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Failed to publish click event for code={}: {}", event.getCode(), e.getMessage());
        }
    }
}
