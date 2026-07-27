package com.vbank.loggingservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vbank.loggingservice.entity.LogEntry;
import com.vbank.loggingservice.entity.LogMessageType;
import com.vbank.loggingservice.exception.MalformedLogMessageException;
import com.vbank.loggingservice.repository.LogEntryRepository;
import com.vbank.loggingservice.service.impl.LogEntryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

class LogEntryServiceImplTest {

    @Mock
    private LogEntryRepository logEntryRepository;

    private LogEntryServiceImpl logEntryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        logEntryService = new LogEntryServiceImpl(logEntryRepository, objectMapper);
    }

    @Test
    void ingest_shouldParseAndPersist_whenMessageIsWellFormed() {
        String rawMessage = "{"
                + "\"message\":\"{\\\"username\\\":\\\"john.doe\\\"}\","
                + "\"messageType\":\"Request\","
                + "\"dateTime\":\"2025-07-15T07:16:49.822Z\""
                + "}";

        logEntryService.ingest(rawMessage);

        ArgumentCaptor<LogEntry> captor = ArgumentCaptor.forClass(LogEntry.class);
        verify(logEntryRepository).save(captor.capture());

        LogEntry saved = captor.getValue();
        assertThat(saved.getMessageType()).isEqualTo(LogMessageType.REQUEST);
        assertThat(saved.getMessage()).contains("john.doe");
    }

    @Test
    void ingest_shouldThrowMalformedLogMessage_whenJsonIsInvalid() {
        String rawMessage = "not valid json at all";

        assertThatThrownBy(() -> logEntryService.ingest(rawMessage))
                .isInstanceOf(MalformedLogMessageException.class);
    }

    @Test
    void ingest_shouldThrowMalformedLogMessage_whenMessageTypeIsUnrecognized() {
        String rawMessage = "{"
                + "\"message\":\"{}\","
                + "\"messageType\":\"SomethingElse\","
                + "\"dateTime\":\"2025-07-15T07:16:49.822Z\""
                + "}";

        assertThatThrownBy(() -> logEntryService.ingest(rawMessage))
                .isInstanceOf(MalformedLogMessageException.class);
    }
}
