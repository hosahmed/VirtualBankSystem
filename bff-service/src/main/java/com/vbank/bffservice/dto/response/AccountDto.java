package com.vbank.bffservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * ASSUMED CONTRACT - not yet verified against the real Account Service.
 * Matches the spec's documented GET /users/{userId}/accounts example
 * exactly (accountId, accountNumber, accountType, balance, status).
 * When your friend's code is available, diff this class's fields
 * against their actual response body before trusting the BFF's
 * output - a silent field rename (e.g. "type" instead of
 * "accountType") will deserialize to null here, not throw an error,
 * which is the dangerous failure mode to watch for.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private UUID accountId;
    private String accountNumber;
    private String accountType;
    private BigDecimal balance;
    private String status;
}
