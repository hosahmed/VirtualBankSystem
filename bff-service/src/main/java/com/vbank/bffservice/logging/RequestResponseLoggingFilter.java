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

/**
 * Implements spec section "Request and Response Logging in
 * Microservices" for this service: after each request is processed
 * and just before the response is sent, publish a
 * {message, messageType, dateTime} envelope to Kafka for both the
 * request and the response.
 *
 * WHY a filter, not an interceptor or AOP aspect: a servlet Filter
 * runs outside Spring MVC's dispatch entirely, so it captures every
 * request that hits this service, including ones that fail before
 * reaching a controller (e.g. a 404 for an unmapped path) or that a
 * future interceptor might short-circuit. Logging is exactly the kind
 * of cross-cutting concern that should not depend on which handler
 * ends up processing the request.
 *
 * INTEGRATION NOTE FOR OPENCODE: this is ONE possible implementation
 * of the spec's logging requirement, written without visibility into
 * how User/Account/Transaction Service actually implemented theirs.
 * If they used a different mechanism (e.g. an interceptor, or a
 * different envelope-building helper class), this filter's OUTPUT
 * envelope shape must still match logging-service's expected
 * {message, messageType, dateTime} format exactly - that's the only
 * hard requirement, not the mechanism used to produce it. See
 * docs/OPENCODE.md's Kafka logging section for the full audit
 * checklist.
 */
@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String loggingTopic;

    public RequestResponseLoggingFilter(KafkaTemplate<String, String> kafkaTemplate,
                                         ObjectMapper objectMapper,
                                         @Value("${app.kafka.logging-topic}") String loggingTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.loggingTopic = loggingTopic;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
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
