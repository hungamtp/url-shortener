package com.urlshortener.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.model.ClickEvent;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ClickEventListener {

    private static final Logger log = LoggerFactory.getLogger(ClickEventListener.class);

    private final ObjectMapper objectMapper;

    public ClickEventListener(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Listens to the url-analytics SQS queue and processes click events.
     * Note: incrementClickCount is also called synchronously in UrlShortenerService.resolve()
     * for real-time accuracy. This listener handles any events that were retried from the DLQ.
     */
    @SqsListener("${app.sqs.analytics-queue:url-analytics}")
    public void onClickEvent(String payload) {
        try {
            ClickEvent event = objectMapper.readValue(payload, ClickEvent.class);
            log.info("Processing click event: code={}, ip={}, ua={}",
                    event.getCode(), event.getIpAddress(), event.getUserAgent());
            // Future: persist to analytics table, aggregate metrics, etc.
        } catch (Exception e) {
            log.error("Failed to process click event: payload={}", payload, e);
            throw new RuntimeException("Click event processing failed", e); // Triggers DLQ retry
        }
    }
}
