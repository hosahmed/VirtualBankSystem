package com.vbank.userservice.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaLoggingProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "logging-topic";

    public KafkaLoggingProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendLog(LogMessage logMessage) {
        kafkaTemplate.send(TOPIC, logMessage);
    }
}
