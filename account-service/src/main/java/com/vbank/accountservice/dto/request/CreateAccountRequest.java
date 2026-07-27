package com.vbank.accountservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {
    @NotNull(message = "User ID is required")
    @Schema(example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private UUID userId;

    @NotBlank(message = "Account type is required")
    @Schema(example = "SAVINGS")
    private String accountType;

    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "0.0", message = "Initial balance must be zero or positive")
    @Schema(example = "100.00")
    private BigDecimal initialBalance;
}
