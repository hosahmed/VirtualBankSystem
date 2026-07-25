package com.vbank.transactionservice.mapper;

import com.vbank.transactionservice.dto.response.TransactionHistoryResponse;
import com.vbank.transactionservice.dto.response.TransferExecutionResponse;
import com.vbank.transactionservice.dto.response.TransferInitiationResponse;
import com.vbank.transactionservice.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransferInitiationResponse toInitiationResponse(Transaction transaction) {
        return TransferInitiationResponse.builder()
                .transactionId(transaction.getTransactionId())
                .status(transaction.getStatus().name().substring(0, 1).toUpperCase() +
                        transaction.getStatus().name().substring(1).toLowerCase())
                .timestamp(transaction.getTimestamp())
                .build();
    }

    public TransferExecutionResponse toExecutionResponse(Transaction transaction) {
        return TransferExecutionResponse.builder()
                .transactionId(transaction.getTransactionId())
                .status(transaction.getStatus().name().substring(0, 1).toUpperCase() +
                        transaction.getStatus().name().substring(1).toLowerCase())
                .timestamp(transaction.getTimestamp())
                .build();
    }

    public TransactionHistoryResponse toHistoryResponse(Transaction transaction) {
        return TransactionHistoryResponse.builder()
                .transactionId(transaction.getTransactionId())
                .fromAccountId(transaction.getFromAccountId())
                .toAccountId(transaction.getToAccountId())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .timestamp(transaction.getTimestamp())
                .deliveryStatus(transaction.getDeliveryStatus().name())
                .build();
    }
}
