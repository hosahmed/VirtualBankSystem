package com.vbank.accountservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetailsResponse {
    @Schema(example = "f1e2d3c4-b5a6-9876-5432-10fedcba9876")
    private UUID accountId;

    @Schema(example = "1234567890")
    private String accountNumber;

    @Schema(example = "SAVINGS")
    private String accountType;

    @Schema(example = "100.00")
    private BigDecimal balance;

    @Schema(example = "ACTIVE")
    private String status;
}
