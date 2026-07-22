package com.vbank.bffservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    @Schema(example = "t1r2a3n4-s5a6-7890-1234-567890abcdef")
    private UUID transactionId;

    @Schema(example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private UUID fromAccountId;

    @Schema(example = "b2c3d4e5-f6a7-8901-2345-67890abcdef1")
    private UUID toAccountId;

    @Schema(example = "50.00")
    private BigDecimal amount;

    @Schema(example = "Cash deposit")
    private String description;

    @Schema(example = "2025-06-30T10:05:00Z")
    private Instant timestamp;

    @Schema(example = "SENT")
    private String deliveryStatus;
}
