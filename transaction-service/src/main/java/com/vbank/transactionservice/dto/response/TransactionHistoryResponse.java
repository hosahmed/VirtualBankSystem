package com.vbank.transactionservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryResponse {
    @Schema(example = "t1r2a3n4-s5a6-7890-1234-567890abcdef")
    private UUID transactionId;

    @Schema(example = "f1e2d3c4-b5a6-9876-5432-10fedcba9876")
    private UUID fromAccountId;

    @Schema(example = "f2e3d3c4-b5a6-9876-5432-10fedbba9876")
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
