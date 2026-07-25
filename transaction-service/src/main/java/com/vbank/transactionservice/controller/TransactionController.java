package com.vbank.transactionservice.controller;

import com.vbank.transactionservice.dto.request.TransferExecutionRequest;
import com.vbank.transactionservice.dto.request.TransferInitiationRequest;
import com.vbank.transactionservice.dto.response.TransactionHistoryResponse;
import com.vbank.transactionservice.dto.response.TransferExecutionResponse;
import com.vbank.transactionservice.dto.response.TransferInitiationResponse;
import com.vbank.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transactions/transfer/initiation")
    public ResponseEntity<TransferInitiationResponse> initiateTransfer(
            @Valid @RequestBody TransferInitiationRequest request) {
        TransferInitiationResponse response = transactionService.initiateTransfer(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transactions/transfer/execution")
    public ResponseEntity<TransferExecutionResponse> executeTransfer(
            @Valid @RequestBody TransferExecutionRequest request) {
        TransferExecutionResponse response = transactionService.executeTransfer(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<List<TransactionHistoryResponse>> getTransactionHistory(
            @PathVariable UUID accountId) {
        List<TransactionHistoryResponse> response = transactionService.getTransactionHistory(accountId);
        return ResponseEntity.ok(response);
    }
}
