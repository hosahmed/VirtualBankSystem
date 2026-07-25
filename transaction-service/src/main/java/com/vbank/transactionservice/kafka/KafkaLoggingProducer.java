package com.vbank.transactionservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaLoggingProducer {

    private final KafkaTemplate<String, LogMessage> kafkaTemplate;
    private static final String TOPIC = "api-logs";

    public void sendLog(LogMessage logMessage) {
        try {
            kafkaTemplate.send(TOPIC, logMessage.getTraceId(), logMessage);
        } catch (Exception e) {
            log.error("Failed to send log to Kafka", e);
        }
    }
}
