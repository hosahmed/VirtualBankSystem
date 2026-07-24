package com.vbank.accountservice.scheduler;

import com.vbank.accountservice.entity.Account;
import com.vbank.accountservice.entity.AccountStatus;
import com.vbank.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StaleAccountScheduler {

    private final AccountRepository accountRepository;

    @Scheduled(fixedRateString = "${scheduler.stale-account.interval:3600000}")
    @Transactional
    public void markStaleAccountsInactive() {
        log.info("Starting scheduled job: markStaleAccountsInactive");

        Instant cutoffTime = Instant.now().minus(24, ChronoUnit.HOURS);

        List<Account> staleAccounts = accountRepository.findByStatusAndLastTransactionDateBefore(
                AccountStatus.ACTIVE, cutoffTime);

        if (staleAccounts.isEmpty()) {
            log.info("No stale accounts found.");
            return;
        }

        for (Account account : staleAccounts) {
            account.setStatus(AccountStatus.INACTIVE);
        }

        accountRepository.saveAll(staleAccounts);

        log.info("Marked {} accounts as INACTIVE due to inactivity.", staleAccounts.size());
    }
}
