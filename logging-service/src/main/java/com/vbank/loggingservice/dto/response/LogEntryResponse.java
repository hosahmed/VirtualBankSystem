package com.vbank.loggingservice.dto.response;

import com.vbank.loggingservice.entity.LogMessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class LogEntryResponse {
    @Schema(example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private UUID id;

    @Schema(example = "{\"userId\":\"a1b2c3d4-...\"}")
    private String message;

    @Schema(example = "Request")
    private LogMessageType messageType;

    @Schema(example = "2025-07-15T07:16:49.822Z")
    private Instant dateTime;

    @Schema(example = "2025-07-15T07:16:50.000Z")
    private Instant receivedAt;
}
