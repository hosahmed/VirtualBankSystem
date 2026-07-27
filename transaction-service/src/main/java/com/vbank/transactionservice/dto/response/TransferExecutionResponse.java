package com.vbank.transactionservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferExecutionResponse {
    @Schema(example = "t1r2a3n4-s5a6-7890-1234-567890abcdef")
    private UUID transactionId;

    @Schema(example = "Success")
    private String status;

    @Schema(example = "2025-07-15T07:16:49.822Z")
    private Instant timestamp;
}
