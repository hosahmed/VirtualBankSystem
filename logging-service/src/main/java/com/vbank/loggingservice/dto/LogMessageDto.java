package com.vbank.loggingservice.dto;

import com.vbank.loggingservice.entity.LogMessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogMessageDto {
    private String message;
    private LogMessageType messageType;
    private Instant dateTime;
}
