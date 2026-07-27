package com.vbank.loggingservice.service;

import com.vbank.loggingservice.dto.LogMessageDto;
import com.vbank.loggingservice.dto.response.LogEntryResponse;
import com.vbank.loggingservice.entity.LogMessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LogEntryService {

    /** Parses and persists one message consumed from Kafka. */
    void ingest(String rawMessage);

    Page<LogEntryResponse> findAll(Pageable pageable);

    Page<LogEntryResponse> findByMessageType(LogMessageType messageType, Pageable pageable);
}
