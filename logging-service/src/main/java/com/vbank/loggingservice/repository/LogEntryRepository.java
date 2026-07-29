package com.vbank.loggingservice.repository;

import com.vbank.loggingservice.entity.LogEntry;
import com.vbank.loggingservice.entity.LogMessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LogEntryRepository extends JpaRepository<LogEntry, UUID> {
    Page<LogEntry> findByMessageType(LogMessageType messageType, Pageable pageable);
}
