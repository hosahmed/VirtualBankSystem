package com.vbank.accountservice.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaLoggingProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaLoggingProducer.class);

    private final KafkaTemplate<String, LogMessage> kafkaTemplate;
    private final String topic;

    public KafkaLoggingProducer(KafkaTemplate<String, LogMessage> kafkaTemplate,
                                @Value("${app.kafka.logging-topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void sendLog(LogMessage logMessage) {
        try {
            kafkaTemplate.send(topic, logMessage);
        } catch (Exception e) {
            log.warn("Failed to publish log message to Kafka", e);
        }
    }
}
