package com.vbank.bffservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class AccountWithTransactionsDto {
    private UUID accountId;
    private String accountNumber;
    private String accountType;
    private BigDecimal balance;
    private List<TransactionDto> transactions;
}
