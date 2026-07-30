package com.vbank.loggingservice.dto;

import com.vbank.loggingservice.entity.LogMessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogMessageDto {
    @NotBlank(message = "Log message cannot be blank")
    private String message;

    @NotNull(message = "Message type is required")
    private LogMessageType messageType;

    @NotNull(message = "Date time is required")
    private Instant dateTime;
}
