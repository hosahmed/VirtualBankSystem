package com.vbank.userservice.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vbank.userservice.kafka.KafkaLoggingProducer;
import com.vbank.userservice.kafka.LogMessage;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final String REDACTED = "******";

    private final KafkaLoggingProducer kafkaLoggingProducer;
    private final ObjectMapper objectMapper;

    public RequestResponseLoggingFilter(KafkaLoggingProducer kafkaLoggingProducer) {
        this.kafkaLoggingProducer = kafkaLoggingProducer;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Providing a cache limit to the constructor to resolve the compilation error
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, 1024 * 10);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        // Proceed with the request, caching the request/response bodies
        filterChain.doFilter(requestWrapper, responseWrapper);

        String requestBody = getStringValue(requestWrapper.getContentAsByteArray(), request.getCharacterEncoding());
        String responseBody = getStringValue(responseWrapper.getContentAsByteArray(), response.getCharacterEncoding());

        String now = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

        if (!requestBody.isBlank()) {
            requestBody = redactSensitiveFields(requestBody);
            if (requestBody.length() > 10000) {
                requestBody = requestBody.substring(0, 10000) + "... [TRUNCATED]";
            }
            kafkaLoggingProducer.sendLog(LogMessage.builder()
                    .message(requestBody)
                    .messageType("Request")
                    .dateTime(now)
                    .build());
        }

        if (!responseBody.isBlank()) {
            if (responseBody.length() > 10000) {
                responseBody = responseBody.substring(0, 10000) + "... [TRUNCATED]";
            }
            kafkaLoggingProducer.sendLog(LogMessage.builder()
                    .message(responseBody)
                    .messageType("Response")
                    .dateTime(now)
                    .build());
        }

        // Extremely important: copy the cached response body back to the actual response output stream!
        responseWrapper.copyBodyToResponse();
    }


    private String redactSensitiveFields(String body) {
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node.isObject() && node.has("password")) {
                ((ObjectNode) node).put("password", REDACTED);
                return objectMapper.writeValueAsString(node);
            }
        } catch (Exception e) {
            // If body is not valid JSON, return as-is
        }
        return body;
    }

    private String getStringValue(byte[] contentAsByteArray, String characterEncoding) {
        try {
            if (contentAsByteArray == null || contentAsByteArray.length == 0) {
                return "";
            }
            return new String(contentAsByteArray, 0, contentAsByteArray.length, characterEncoding != null ? characterEncoding : "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }
}
