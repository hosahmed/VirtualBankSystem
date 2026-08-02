package com.vbank.transactionservice.controller;

import com.vbank.transactionservice.dto.request.TransferExecutionRequest;
import com.vbank.transactionservice.dto.request.TransferInitiationRequest;
import com.vbank.transactionservice.dto.response.TransactionHistoryResponse;
import com.vbank.transactionservice.dto.response.TransferExecutionResponse;
import com.vbank.transactionservice.dto.response.TransferInitiationResponse;
import com.vbank.transactionservice.exception.ErrorResponse;
import com.vbank.transactionservice.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Transaction Service", description = "Financial transfers and transaction history")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PreAuthorize("hasRole('ADMIN') or @transactionSecurity.isOwner(authentication.principal, #request.fromAccountId)")
    @PostMapping("/transactions/transfer/initiation")
    @Operation(summary = "Initiate a fund transfer")
    @ApiResponse(responseCode = "200", description = "Transfer initiated",
            content = @Content(schema = @Schema(implementation = TransferInitiationResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid accounts or insufficient funds",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<TransferInitiationResponse> initiateTransfer(
            @Valid @RequestBody TransferInitiationRequest request) {
        TransferInitiationResponse response = transactionService.initiateTransfer(request);
        return ResponseEntity.ok(response);
    }

    // This is an internal-only endpoint called by the Saga orchestrator or directly, 
    // but in our simplified architecture we might allow ADMIN to execute or it's called by internal mechanisms. 
    // For safety, require ADMIN or internal system (not implemented here, so ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/transactions/transfer/execution")
    @Operation(summary = "Execute an initiated transfer")
    @ApiResponse(responseCode = "200", description = "Transfer executed",
            content = @Content(schema = @Schema(implementation = TransferExecutionResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid transaction ID or insufficient funds",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<TransferExecutionResponse> executeTransfer(
            @Valid @RequestBody TransferExecutionRequest request) {
        TransferExecutionResponse response = transactionService.executeTransfer(request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or @transactionSecurity.isOwner(authentication.principal, #accountId)")
    @GetMapping("/accounts/{accountId}/transactions")
    @Operation(summary = "Get transaction history for an account")
    @ApiResponse(responseCode = "200", description = "Transaction history")
    @ApiResponse(responseCode = "404", description = "No transactions found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<List<TransactionHistoryResponse>> getTransactionHistory(
            @PathVariable UUID accountId) {
        List<TransactionHistoryResponse> response = transactionService.getTransactionHistory(accountId);
        return ResponseEntity.ok(response);
    }
}
