package com.vbank.transactionservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferExecutionRequest {
    @NotNull(message = "Transaction ID is required")
    @Schema(example = "t1r2a3n4-s5a6-7890-1234-567890abcdef")
    private UUID transactionId;
}
