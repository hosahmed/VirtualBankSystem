package com.vbank.bffservice.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String loggingTopic;

    public RequestResponseLoggingFilter(KafkaTemplate<String, String> kafkaTemplate,
                                         @Value("${app.kafka.logging-topic}") String loggingTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
        this.loggingTopic = loggingTopic;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui") || uri.startsWith("/webjars")) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, 1024 * 10);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        Instant requestTime = Instant.now();
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            Instant responseTime = Instant.now();

            publish(requestBodyOrFallback(wrappedRequest), "Request", requestTime);
            publish(new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8),
                    "Response", responseTime);

            // CRITICAL: without this, the client receives an empty
            // body - ContentCachingResponseWrapper buffers the body
            // instead of writing it through, so it must be copied back
            // to the real response after we're done reading it.
            wrappedResponse.copyBodyToResponse();
        }
    }

    /**
     * This service's only current endpoint (GET /bff/dashboard/{userId})
     * has no request body, so there's nothing meaningful to capture
     * from ContentCachingRequestWrapper for it. Falling back to
     * method + URI keeps the log entry useful rather than an empty
     * string. If a future POST/PUT endpoint is added to this service
     * with an actual body, that body will be captured here instead
     * (once something in the request chain actually reads it - see
     * the class-level Javadoc caveat about ContentCachingRequestWrapper
     * only buffering what's actually consumed downstream).
     */
    private String requestBodyOrFallback(ContentCachingRequestWrapper wrappedRequest) {
        byte[] body = wrappedRequest.getContentAsByteArray();
        if (body.length > 0) {
            return new String(body, StandardCharsets.UTF_8);
        }
        return "{\"method\":\"" + wrappedRequest.getMethod() + "\",\"uri\":\""
                + wrappedRequest.getRequestURI() + "\"}";
    }

    private void publish(String content, String messageType, Instant dateTime) {
        try {
            // Truncate overly large payloads (like Swagger UI assets) to prevent
            // Kafka RecordTooLargeException and DB TEXT column truncation errors.
            if (content != null && content.length() > 10000) {
                content = content.substring(0, 10000) + "... [TRUNCATED]";
            }

            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("message", content);
            envelope.put("messageType", messageType);
            envelope.put("dateTime", dateTime.toString());

            String json = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send(loggingTopic, json);
        } catch (Exception ex) {
            // A logging failure must never fail the actual HTTP
            // request/response it's trying to log - swallow and
            // report server-side only.
            log.warn("Failed to publish {} log message to Kafka", messageType, ex);
        }
    }
}
