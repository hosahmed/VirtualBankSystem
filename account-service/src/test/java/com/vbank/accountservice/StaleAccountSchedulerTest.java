package com.vbank.accountservice;

import com.vbank.accountservice.entity.Account;
import com.vbank.accountservice.entity.AccountStatus;
import com.vbank.accountservice.repository.AccountRepository;
import com.vbank.accountservice.scheduler.StaleAccountScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class StaleAccountSchedulerTest {

    @Mock
    private AccountRepository accountRepository;

    private StaleAccountScheduler staleAccountScheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        staleAccountScheduler = new StaleAccountScheduler(accountRepository);
    }

    @Test
    void markStaleAccountsInactive_shouldUpdateStatus_whenStaleAccountsFound() {
        Account staleAccount1 = new Account();
        staleAccount1.setStatus(AccountStatus.ACTIVE);

        Account staleAccount2 = new Account();
        staleAccount2.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findByStatusAndLastTransactionDateBefore(
                eq(AccountStatus.ACTIVE), any(Instant.class)))
                .thenReturn(List.of(staleAccount1, staleAccount2));

        staleAccountScheduler.markStaleAccountsInactive();

        assertThat(staleAccount1.getStatus()).isEqualTo(AccountStatus.INACTIVE);
        assertThat(staleAccount2.getStatus()).isEqualTo(AccountStatus.INACTIVE);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Account>> captor = ArgumentCaptor.forClass(List.class);
        verify(accountRepository).saveAll(captor.capture());

        List<Account> savedAccounts = captor.getValue();
        assertThat(savedAccounts).hasSize(2);
    }

    @Test
    void markStaleAccountsInactive_shouldDoNothing_whenNoStaleAccountsFound() {
        when(accountRepository.findByStatusAndLastTransactionDateBefore(
                eq(AccountStatus.ACTIVE), any(Instant.class)))
                .thenReturn(List.of());

        staleAccountScheduler.markStaleAccountsInactive();

        verify(accountRepository, never()).saveAll(any());
    }
}
