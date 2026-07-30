package com.vbank.accountservice.service.impl;

import com.vbank.accountservice.dto.request.CreateAccountRequest;
import com.vbank.accountservice.dto.request.TransferRequest;
import com.vbank.accountservice.dto.response.AccountCreatedResponse;
import com.vbank.accountservice.dto.response.AccountDetailsResponse;
import com.vbank.accountservice.dto.response.TransferResponse;
import com.vbank.accountservice.entity.Account;
import com.vbank.accountservice.entity.AccountStatus;
import com.vbank.accountservice.entity.AccountType;
import com.vbank.accountservice.exception.AccountNotFoundException;
import com.vbank.accountservice.exception.InsufficientFundsException;
import com.vbank.accountservice.exception.InvalidAccountRequestException;
import com.vbank.accountservice.mapper.AccountMapper;
import com.vbank.accountservice.repository.AccountRepository;
import com.vbank.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vbank.accountservice.client.UserServiceClient;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final UserServiceClient userServiceClient;

    @Override
    @Transactional
    public AccountCreatedResponse createAccount(CreateAccountRequest request) {
        AccountType type;
        try {
            type = AccountType.valueOf(request.getAccountType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidAccountRequestException("Invalid account type: " + request.getAccountType());
        }

        if (type != AccountType.SAVINGS && type != AccountType.CHECKING) {
            throw new InvalidAccountRequestException("Account type must be SAVINGS or CHECKING");
        }

        userServiceClient.validateUserExists(request.getUserId());

        String accountNumber = generateUniqueAccountNumber();

        Account account = Account.builder()
                .userId(request.getUserId())
                .accountNumber(accountNumber)
                .accountType(type)
                .balance(request.getInitialBalance())
                .status(AccountStatus.ACTIVE)
                .lastTransactionDate(Instant.now())
                .build();

        Account savedAccount = accountRepository.save(account);

        return accountMapper.toCreatedResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDetailsResponse getAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));
        return accountMapper.toDetailsResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountDetailsResponse> getAccountsByUserId(UUID userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);
        if (accounts.isEmpty()) {
            throw new AccountNotFoundException("No accounts found for user ID: " + userId);
        }
        return accounts.stream()
                .map(accountMapper::toDetailsResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new InvalidAccountRequestException("Cannot transfer to the same account");
        }

        Account fromAccount = accountRepository.findById(request.getFromAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Source account not found"));

        Account toAccount = accountRepository.findById(request.getToAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Destination account not found"));

        if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidAccountRequestException("Source account is inactive");
        }

        if (toAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidAccountRequestException("Destination account is inactive");
        }

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds in the source account");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        Instant now = Instant.now();
        fromAccount.setLastTransactionDate(now);
        toAccount.setLastTransactionDate(now);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        return TransferResponse.builder()
                .message("Account updated successfully.")
                .build();
    }

    private String generateUniqueAccountNumber() {
        Random random = new Random();
        // Generates a 10 digit number
        long number = (long) (Math.random() * 9000000000L) + 1000000000L;
        return String.valueOf(number);
    }
}
