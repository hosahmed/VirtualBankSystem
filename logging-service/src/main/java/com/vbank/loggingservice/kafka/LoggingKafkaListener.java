package com.vbank.loggingservice.kafka;

import com.vbank.loggingservice.exception.MalformedLogMessageException;
import com.vbank.loggingservice.service.LogEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class LoggingKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(LoggingKafkaListener.class);

    private final LogEntryService logEntryService;

    public LoggingKafkaListener(LogEntryService logEntryService) {
        this.logEntryService = logEntryService;
    }

    @KafkaListener(topics = "${app.kafka.logging-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String rawMessage) {
        try {
            logEntryService.ingest(rawMessage);
        } catch (MalformedLogMessageException ex) {
            log.warn("Discarding malformed log message: {}", ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error persisting log message", ex);
        }
    }
}
