package com.vbank.accountservice.dto.request;

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
public class TransferRequest {

    @NotNull(message = "From Account ID is required")
    @Schema(example = "f1e2d3c4-b5a6-9876-5432-10fedcba9876")
    private UUID fromAccountId;

    @NotNull(message = "To Account ID is required")
    @Schema(example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private UUID toAccountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Schema(example = "100.00")
    private BigDecimal amount;
}
