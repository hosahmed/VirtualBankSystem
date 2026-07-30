package com.vbank.transactionservice.filter;

import com.vbank.transactionservice.kafka.KafkaLoggingProducer;
import com.vbank.transactionservice.kafka.LogMessage;
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

    private final KafkaLoggingProducer kafkaLoggingProducer;

    public RequestResponseLoggingFilter(KafkaLoggingProducer kafkaLoggingProducer) {
        this.kafkaLoggingProducer = kafkaLoggingProducer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui")) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, 1024 * 10);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        filterChain.doFilter(requestWrapper, responseWrapper);

        String requestBody = getStringValue(requestWrapper.getContentAsByteArray(), request.getCharacterEncoding());
        String responseBody = getStringValue(responseWrapper.getContentAsByteArray(), response.getCharacterEncoding());

        String now = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

        if (!requestBody.isBlank()) {
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

        responseWrapper.copyBodyToResponse();
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
