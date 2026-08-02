package com.vbank.accountservice.security;

import com.vbank.accountservice.entity.Account;
import com.vbank.accountservice.repository.AccountRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("accountSecurity")
public class AccountSecurity {

    private final AccountRepository accountRepository;

    public AccountSecurity(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public boolean isOwner(String principal, UUID accountId) {
        if (principal == null || accountId == null) {
            return false;
        }
        
        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null) {
            return false; // Not found, so not owner. Controller will return 404 or 403.
        }
        
        return account.getUserId().toString().equals(principal);
    }
}
