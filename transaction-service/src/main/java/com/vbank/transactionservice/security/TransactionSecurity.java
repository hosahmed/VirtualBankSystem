package com.vbank.transactionservice.security;

import com.vbank.transactionservice.client.AccountServiceClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component("transactionSecurity")
public class TransactionSecurity {

    private final AccountServiceClient accountServiceClient;

    public TransactionSecurity(AccountServiceClient accountServiceClient) {
        this.accountServiceClient = accountServiceClient;
    }

    public boolean isOwner(String principal, UUID accountId) {
        if (principal == null || accountId == null) {
            return false;
        }

        try {
            Map<String, Object> account = accountServiceClient.getAccount(accountId);
            if (account != null && account.containsKey("userId")) {
                return account.get("userId").toString().equals(principal);
            }
        } catch (Exception e) {
            // Log and fall through to return false
        }
        
        return false;
    }
}
