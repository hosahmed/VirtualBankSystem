package com.vbank.loggingservice.controller;

import com.vbank.loggingservice.dto.response.LogEntryResponse;
import com.vbank.loggingservice.entity.LogMessageType;
import com.vbank.loggingservice.exception.ErrorResponse;
import com.vbank.loggingservice.service.LogEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vbank.loggingservice.dto.LogMessageDto;

@RestController
@RequestMapping("/logs")
@Tag(name = "Logs", description = "Read-only queries over the log dump table (addition beyond spec minimum)")
public class LogController {

    private final LogEntryService logEntryService;

    public LogController(LogEntryService logEntryService) {
        this.logEntryService = logEntryService;
    }
    @GetMapping
    @Operation(summary = "List log entries",
            description = "Optionally filter by messageType (Request/Response). Paginated.")
    @ApiResponse(responseCode = "200", description = "Paginated list of log entries",
            content = @Content(schema = @Schema(implementation = LogEntryResponse.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Page<LogEntryResponse>> getLogs(
            @RequestParam(required = false) String messageType,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LogMessageType type = messageType == null ? null
                : LogMessageType.fromWireValue(messageType);
        Page<LogEntryResponse> page = type == null
                ? logEntryService.findAll(pageable)
                : logEntryService.findByMessageType(type, pageable);
        return ResponseEntity.ok(page);
    }

    @PostMapping
    @Operation(summary = "Test log ingestion",
            description = "Manually push a log message via HTTP as if it came from Kafka")
    @ApiResponse(responseCode = "200", description = "Log ingested successfully")
    public ResponseEntity<String> testIngestLog(@RequestBody LogMessageDto logMessageDto) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String rawMessage = mapper.writeValueAsString(logMessageDto);
        logEntryService.ingest(rawMessage);
        return ResponseEntity.ok("Log ingested successfully via HTTP");
    }
}
