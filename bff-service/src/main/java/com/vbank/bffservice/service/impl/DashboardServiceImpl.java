package com.vbank.bffservice.service.impl;

import com.vbank.bffservice.client.AccountServiceClient;
import com.vbank.bffservice.client.TransactionServiceClient;
import com.vbank.bffservice.client.UserServiceClient;
import com.vbank.bffservice.dto.response.AccountDto;
import com.vbank.bffservice.dto.response.AccountWithTransactionsDto;
import com.vbank.bffservice.dto.response.DashboardResponse;
import com.vbank.bffservice.dto.response.UserProfileDto;
import com.vbank.bffservice.exception.DownstreamServiceException;
import com.vbank.bffservice.service.DashboardService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Duration AGGREGATION_TIMEOUT = Duration.ofSeconds(5);

    private final UserServiceClient userServiceClient;
    private final AccountServiceClient accountServiceClient;
    private final TransactionServiceClient transactionServiceClient;

    public DashboardServiceImpl(UserServiceClient userServiceClient,
                                 AccountServiceClient accountServiceClient,
                                 TransactionServiceClient transactionServiceClient) {
        this.userServiceClient = userServiceClient;
        this.accountServiceClient = accountServiceClient;
        this.transactionServiceClient = transactionServiceClient;
    }

    @Override
    public DashboardResponse getDashboard(UUID userId) {
        Mono<UserProfileDto> profileMono = userServiceClient.getProfile(userId);

        Mono<List<AccountWithTransactionsDto>> accountsWithTransactionsMono =
                accountServiceClient.getAccountsForUser(userId)
                        .flatMap(this::withTransactions, /* concurrency */ 8)
                        .collectList();

        // Mono.zip runs the profile call and the accounts-with-
        // transactions pipeline concurrently - the profile call
        // doesn't wait for accounts to finish, and vice versa. Within
        // the accounts pipeline, flatMap above already fans out one
        // transaction-history call per account in parallel (bounded to
        // 8 concurrent calls so one user with many accounts can't
        // open unbounded connections to Transaction Service).
        return Mono.zip(profileMono, accountsWithTransactionsMono)
                .map(tuple -> buildResponse(tuple.getT1(), tuple.getT2()))
                .timeout(AGGREGATION_TIMEOUT)
                .onErrorMap(java.util.concurrent.TimeoutException.class,
                        ex -> new DownstreamServiceException("Dashboard aggregation timed out.", ex))
                .block();
    }

    private Mono<AccountWithTransactionsDto> withTransactions(AccountDto account) {
        return transactionServiceClient.getTransactionsForAccount(account.getAccountId())
                .collectList()
                .map(transactions -> AccountWithTransactionsDto.builder()
                        .accountId(account.getAccountId())
                        .accountNumber(account.getAccountNumber())
                        .accountType(account.getAccountType())
                        .balance(account.getBalance())
                        .transactions(transactions)
                        .build());
    }

    private DashboardResponse buildResponse(UserProfileDto profile,
                                             List<AccountWithTransactionsDto> accounts) {
        return DashboardResponse.builder()
                .userId(profile.getUserId())
                .username(profile.getUsername())
                .email(profile.getEmail())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .accounts(accounts)
                .build();
    }
}
