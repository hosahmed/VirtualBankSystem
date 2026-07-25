package com.vbank.transactionservice.service.impl;

import com.vbank.transactionservice.client.AccountServiceClient;
import com.vbank.transactionservice.dto.request.TransferExecutionRequest;
import com.vbank.transactionservice.dto.request.TransferInitiationRequest;
import com.vbank.transactionservice.dto.response.TransactionHistoryResponse;
import com.vbank.transactionservice.dto.response.TransferExecutionResponse;
import com.vbank.transactionservice.dto.response.TransferInitiationResponse;
import com.vbank.transactionservice.entity.DeliveryStatus;
import com.vbank.transactionservice.entity.Transaction;
import com.vbank.transactionservice.entity.TransactionStatus;
import com.vbank.transactionservice.exception.InvalidTransactionException;
import com.vbank.transactionservice.exception.TransactionNotFoundException;
import com.vbank.transactionservice.mapper.TransactionMapper;
import com.vbank.transactionservice.repository.TransactionRepository;
import com.vbank.transactionservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional
    public TransferInitiationResponse initiateTransfer(TransferInitiationRequest request) {
        // Validate accounts exist via Account Service
        accountServiceClient.validateAccountExists(request.getFromAccountId());
        accountServiceClient.validateAccountExists(request.getToAccountId());

        // Create and save the transaction with INITIATED status
        Transaction transaction = Transaction.builder()
                .fromAccountId(request.getFromAccountId())
                .toAccountId(request.getToAccountId())
                .amount(request.getAmount())
                .description(request.getDescription())
                .status(TransactionStatus.INITIATED)
                .deliveryStatus(DeliveryStatus.SENT)
                .timestamp(Instant.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Transaction initiated: {}", savedTransaction.getTransactionId());

        return transactionMapper.toInitiationResponse(savedTransaction);
    }

    @Override
    @Transactional
    public TransferExecutionResponse executeTransfer(TransferExecutionRequest request) {
        Transaction transaction = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new TransactionNotFoundException(
                        "Transaction not found with ID: " + request.getTransactionId()));

        if (transaction.getStatus() != TransactionStatus.INITIATED) {
            throw new InvalidTransactionException(
                    "Transaction has already been processed. Current status: " + transaction.getStatus());
        }

        try {
            // Call Account Service to execute the actual transfer
            accountServiceClient.executeTransfer(
                    transaction.getFromAccountId(),
                    transaction.getToAccountId(),
                    transaction.getAmount());

            // Update status to SUCCESS
            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setDeliveryStatus(DeliveryStatus.DELIVERED);

            log.info("Transaction executed successfully: {}", transaction.getTransactionId());
        } catch (InvalidTransactionException e) {
            // Update status to FAILED
            transaction.setStatus(TransactionStatus.FAILED);

            log.error("Transaction execution failed: {}", transaction.getTransactionId());

            transactionRepository.save(transaction);
            throw e;
        }

        Transaction updatedTransaction = transactionRepository.save(transaction);

        return transactionMapper.toExecutionResponse(updatedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionHistoryResponse> getTransactionHistory(UUID accountId) {
        List<Transaction> transactions = transactionRepository
                .findByFromAccountIdOrToAccountIdOrderByTimestampDesc(accountId, accountId);

        if (transactions.isEmpty()) {
            throw new TransactionNotFoundException(
                    "No transactions found for account ID " + accountId + ".");
        }

        return transactions.stream()
                .map(transactionMapper::toHistoryResponse)
                .collect(Collectors.toList());
    }
}
