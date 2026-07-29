package com.vbank.loggingservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbank.loggingservice.dto.LogMessageDto;
import com.vbank.loggingservice.dto.response.LogEntryResponse;
import com.vbank.loggingservice.entity.LogEntry;
import com.vbank.loggingservice.entity.LogMessageType;
import com.vbank.loggingservice.exception.MalformedLogMessageException;
import com.vbank.loggingservice.repository.LogEntryRepository;
import com.vbank.loggingservice.service.LogEntryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class LogEntryServiceImpl implements LogEntryService {

    private final LogEntryRepository logEntryRepository;
    private final ObjectMapper objectMapper;

    public LogEntryServiceImpl(LogEntryRepository logEntryRepository, ObjectMapper objectMapper) {
        this.logEntryRepository = logEntryRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void ingest(String rawMessage) {
        LogMessageDto dto;
        try {
            dto = objectMapper.readValue(rawMessage, LogMessageDto.class);
        } catch (Exception ex) {
            throw new MalformedLogMessageException("Could not parse log message: " + rawMessage, ex);
        }

        LogEntry entry = LogEntry.builder()
                .message(dto.getMessage())
                .messageType(dto.getMessageType())
                .dateTime(dto.getDateTime())
                .build();

        logEntryRepository.save(entry);
    }

    @Override
    public Page<LogEntryResponse> findAll(Pageable pageable) {
        return logEntryRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public Page<LogEntryResponse> findByMessageType(LogMessageType messageType, Pageable pageable) {
        return logEntryRepository.findByMessageType(messageType, pageable).map(this::toResponse);
    }

    private LogEntryResponse toResponse(LogEntry entry) {
        return LogEntryResponse.builder()
                .id(entry.getId())
                .message(entry.getMessage())
                .messageType(entry.getMessageType())
                .dateTime(entry.getDateTime())
                .receivedAt(entry.getReceivedAt())
                .build();
    }
}
