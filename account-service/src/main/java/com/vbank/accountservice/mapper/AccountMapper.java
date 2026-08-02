package com.vbank.accountservice.mapper;

import com.vbank.accountservice.dto.response.AccountCreatedResponse;
import com.vbank.accountservice.dto.response.AccountDetailsResponse;
import com.vbank.accountservice.entity.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountCreatedResponse toCreatedResponse(Account account) {
        return AccountCreatedResponse.builder()
                .accountId(account.getAccountId())
                .accountNumber(account.getAccountNumber())
                .message("Account created successfully.")
                .build();
    }

    public AccountDetailsResponse toDetailsResponse(Account account) {
        return AccountDetailsResponse.builder()
                .accountId(account.getAccountId())
                .userId(account.getUserId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType().name())
                .balance(account.getBalance())
                .status(account.getStatus().name())
                .build();
    }
}
