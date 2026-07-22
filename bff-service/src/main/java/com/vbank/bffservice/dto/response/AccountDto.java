package com.vbank.bffservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    @Schema(example = "f1e2d3c4-b5a6-9876-5432-10fedcba9876")
    private UUID accountId;

    @Schema(example = "1234567890")
    private String accountNumber;

    @Schema(example = "SAVINGS")
    private String accountType;

    @Schema(example = "120.00")
    private BigDecimal balance;

    @Schema(example = "ACTIVE")
    private String status;
}
