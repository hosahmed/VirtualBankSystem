package com.vbank.transactionservice.service;

import com.vbank.transactionservice.dto.request.TransferExecutionRequest;
import com.vbank.transactionservice.dto.request.TransferInitiationRequest;
import com.vbank.transactionservice.dto.response.TransactionHistoryResponse;
import com.vbank.transactionservice.dto.response.TransferExecutionResponse;
import com.vbank.transactionservice.dto.response.TransferInitiationResponse;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
    TransferInitiationResponse initiateTransfer(TransferInitiationRequest request);
    TransferExecutionResponse executeTransfer(TransferExecutionRequest request);
    List<TransactionHistoryResponse> getTransactionHistory(UUID accountId);
}
