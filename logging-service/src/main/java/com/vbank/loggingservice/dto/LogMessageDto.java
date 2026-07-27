package com.vbank.loggingservice.dto;

import com.vbank.loggingservice.entity.LogMessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Deserialization target for the Kafka message body every other
 * microservice publishes, per the spec's exact format:
 * { "message": "...", "messageType": "Request"|"Response", "dateTime": "..." }
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogMessageDto {
    private String message;
    private LogMessageType messageType;
    private Instant dateTime;
}
