package com.vbank.bffservice;

import com.vbank.bffservice.client.AccountServiceClient;
import com.vbank.bffservice.client.TransactionServiceClient;
import com.vbank.bffservice.client.UserServiceClient;
import com.vbank.bffservice.dto.response.AccountDto;
import com.vbank.bffservice.dto.response.DashboardResponse;
import com.vbank.bffservice.dto.response.TransactionDto;
import com.vbank.bffservice.dto.response.UserProfileDto;
import com.vbank.bffservice.exception.DownstreamServiceException;
import com.vbank.bffservice.exception.UpstreamUserNotFoundException;
import com.vbank.bffservice.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class DashboardServiceImplTest {

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private AccountServiceClient accountServiceClient;

    @Mock
    private TransactionServiceClient transactionServiceClient;

    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dashboardService = new DashboardServiceImpl(userServiceClient, accountServiceClient, transactionServiceClient);
    }

    @Test
    void getDashboard_shouldAggregateProfileAccountsAndTransactions() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        UserProfileDto profile = new UserProfileDto(userId, "john.doe", "john.doe@example.com", "John", "Doe");
        AccountDto account = new AccountDto(accountId, "1234567890", "SAVINGS", BigDecimal.valueOf(120.00), "ACTIVE");
        TransactionDto transaction = new TransactionDto(
                UUID.randomUUID(), null, accountId, BigDecimal.valueOf(50.00),
                "Cash deposit", java.time.Instant.now(), "SENT");

        when(userServiceClient.getProfile(userId)).thenReturn(Mono.just(profile));
        when(accountServiceClient.getAccountsForUser(userId)).thenReturn(Flux.just(account));
        when(transactionServiceClient.getTransactionsForAccount(accountId)).thenReturn(Flux.just(transaction));

        DashboardResponse response = dashboardService.getDashboard(userId);

        assertThat(response.getUsername()).isEqualTo("john.doe");
        assertThat(response.getAccounts()).hasSize(1);
        assertThat(response.getAccounts().get(0).getTransactions()).hasSize(1);
        assertThat(response.getAccounts().get(0).getTransactions().get(0).getDescription())
                .isEqualTo("Cash deposit");
    }

    @Test
    void getDashboard_shouldReturnEmptyAccountsList_whenUserHasNoAccounts() {
        UUID userId = UUID.randomUUID();
        UserProfileDto profile = new UserProfileDto(userId, "new.user", "new.user@example.com", "New", "User");

        when(userServiceClient.getProfile(userId)).thenReturn(Mono.just(profile));
        // Mirrors AccountServiceClient's own 404-to-empty-Flux translation.
        when(accountServiceClient.getAccountsForUser(userId)).thenReturn(Flux.empty());

        DashboardResponse response = dashboardService.getDashboard(userId);

        assertThat(response.getAccounts()).isEmpty();
    }

    @Test
    void getDashboard_shouldPropagateUserNotFound_whenProfileCallFails() {
        UUID userId = UUID.randomUUID();
        when(userServiceClient.getProfile(userId))
                .thenReturn(Mono.error(new UpstreamUserNotFoundException("not found")));
        when(accountServiceClient.getAccountsForUser(userId)).thenReturn(Flux.empty());

        assertThatThrownBy(() -> dashboardService.getDashboard(userId))
                .isInstanceOf(UpstreamUserNotFoundException.class);
    }

    @Test
    void getDashboard_shouldPropagateDownstreamFailure_whenAccountServiceFails() {
        UUID userId = UUID.randomUUID();
        UserProfileDto profile = new UserProfileDto(userId, "john.doe", "john.doe@example.com", "John", "Doe");

        when(userServiceClient.getProfile(userId)).thenReturn(Mono.just(profile));
        when(accountServiceClient.getAccountsForUser(userId))
                .thenReturn(Flux.error(new DownstreamServiceException("Account Service down", new RuntimeException())));

        assertThatThrownBy(() -> dashboardService.getDashboard(userId))
                .isInstanceOf(DownstreamServiceException.class);
    }
}
