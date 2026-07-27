package com.vbank.transactionservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferInitiationRequest {
    @NotNull(message = "From Account ID is required")
    @Schema(example = "f1e2d3c4-b5a6-9876-5432-10fedcba9876")
    private UUID fromAccountId;

    @NotNull(message = "To Account ID is required")
    @Schema(example = "g7h8i9j0-k1l2-3456-7890-abcdef123456")
    private UUID toAccountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Schema(example = "30.00")
    private BigDecimal amount;

    @Schema(example = "Transfer to checking account")
    private String description;
}
