package com.vbank.transactionservice.security;

import com.vbank.transactionservice.client.AccountServiceClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component("transactionSecurity")
public class TransactionSecurity {

    private final AccountServiceClient accountServiceClient;
    private final com.vbank.transactionservice.repository.TransactionRepository transactionRepository;

    public TransactionSecurity(AccountServiceClient accountServiceClient, com.vbank.transactionservice.repository.TransactionRepository transactionRepository) {
        this.accountServiceClient = accountServiceClient;
        this.transactionRepository = transactionRepository;
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
            System.err.println("Error in TransactionSecurity.isOwner calling account-service:");
            e.printStackTrace();
        }
        
        return false;
    }

    public boolean isTransactionOwner(String principal, UUID transactionId) {
        if (principal == null || transactionId == null) {
            return false;
        }

        com.vbank.transactionservice.entity.Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
        if (transaction == null) {
            return false;
        }

        return isOwner(principal, transaction.getFromAccountId());
    }
}
