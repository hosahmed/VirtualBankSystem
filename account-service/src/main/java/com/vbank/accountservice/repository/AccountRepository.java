package com.vbank.accountservice.repository;

import com.vbank.accountservice.entity.Account;
import com.vbank.accountservice.entity.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByUserId(UUID userId);
    List<Account> findByStatusAndLastTransactionDateBefore(AccountStatus status, Instant cutoff);
}
