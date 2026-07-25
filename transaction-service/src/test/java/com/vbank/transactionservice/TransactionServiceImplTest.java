package com.vbank.transactionservice;

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
import com.vbank.transactionservice.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountServiceClient accountServiceClient;

    @Mock
    private TransactionMapper transactionMapper;

    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transactionService = new TransactionServiceImpl(transactionRepository, accountServiceClient, transactionMapper);
    }

    // ---- initiateTransfer() ----

    @Test
    void initiateTransfer_shouldCreateTransaction_whenAccountsAreValid() {
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();
        TransferInitiationRequest request = new TransferInitiationRequest(
                fromAccountId, toAccountId, new BigDecimal("30.00"), "Transfer to checking account");

        doNothing().when(accountServiceClient).validateAccountExists(fromAccountId);
        doNothing().when(accountServiceClient).validateAccountExists(toAccountId);

        Transaction savedTransaction = Transaction.builder()
                .transactionId(UUID.randomUUID())
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(new BigDecimal("30.00"))
                .description("Transfer to checking account")
                .status(TransactionStatus.INITIATED)
                .deliveryStatus(DeliveryStatus.SENT)
                .timestamp(Instant.now())
                .build();
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        TransferInitiationResponse expectedResponse = TransferInitiationResponse.builder()
                .transactionId(savedTransaction.getTransactionId())
                .status("Initiated")
                .timestamp(savedTransaction.getTimestamp())
                .build();
        when(transactionMapper.toInitiationResponse(savedTransaction)).thenReturn(expectedResponse);

        TransferInitiationResponse response = transactionService.initiateTransfer(request);

        assertThat(response.getStatus()).isEqualTo("Initiated");
        assertThat(response.getTransactionId()).isEqualTo(savedTransaction.getTransactionId());

        verify(accountServiceClient).validateAccountExists(fromAccountId);
        verify(accountServiceClient).validateAccountExists(toAccountId);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void initiateTransfer_shouldThrowException_whenFromAccountNotFound() {
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();
        TransferInitiationRequest request = new TransferInitiationRequest(
                fromAccountId, toAccountId, new BigDecimal("30.00"), "Test");

        doThrow(new InvalidTransactionException("Account not found: " + fromAccountId))
                .when(accountServiceClient).validateAccountExists(fromAccountId);

        assertThatThrownBy(() -> transactionService.initiateTransfer(request))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("Account not found");
    }

    @Test
    void initiateTransfer_shouldThrowException_whenToAccountNotFound() {
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();
        TransferInitiationRequest request = new TransferInitiationRequest(
                fromAccountId, toAccountId, new BigDecimal("30.00"), "Test");

        doNothing().when(accountServiceClient).validateAccountExists(fromAccountId);
        doThrow(new InvalidTransactionException("Account not found: " + toAccountId))
                .when(accountServiceClient).validateAccountExists(toAccountId);

        assertThatThrownBy(() -> transactionService.initiateTransfer(request))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("Account not found");
    }

    // ---- executeTransfer() ----

    @Test
    void executeTransfer_shouldSucceed_whenTransactionIsInitiated() {
        UUID transactionId = UUID.randomUUID();
        TransferExecutionRequest request = new TransferExecutionRequest(transactionId);

        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .fromAccountId(UUID.randomUUID())
                .toAccountId(UUID.randomUUID())
                .amount(new BigDecimal("30.00"))
                .status(TransactionStatus.INITIATED)
                .deliveryStatus(DeliveryStatus.SENT)
                .timestamp(Instant.now())
                .build();

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        doNothing().when(accountServiceClient).executeTransfer(
                transaction.getFromAccountId(), transaction.getToAccountId(), transaction.getAmount());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransferExecutionResponse expectedResponse = TransferExecutionResponse.builder()
                .transactionId(transactionId)
                .status("Success")
                .timestamp(transaction.getTimestamp())
                .build();
        when(transactionMapper.toExecutionResponse(any(Transaction.class))).thenReturn(expectedResponse);

        TransferExecutionResponse response = transactionService.executeTransfer(request);

        assertThat(response.getStatus()).isEqualTo("Success");
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(transaction.getDeliveryStatus()).isEqualTo(DeliveryStatus.DELIVERED);
    }

    @Test
    void executeTransfer_shouldThrowException_whenTransactionNotFound() {
        UUID transactionId = UUID.randomUUID();
        TransferExecutionRequest request = new TransferExecutionRequest(transactionId);

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.executeTransfer(request))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining("Transaction not found");
    }

    @Test
    void executeTransfer_shouldThrowException_whenTransactionAlreadyProcessed() {
        UUID transactionId = UUID.randomUUID();
        TransferExecutionRequest request = new TransferExecutionRequest(transactionId);

        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .status(TransactionStatus.SUCCESS)
                .build();

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> transactionService.executeTransfer(request))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("already been processed");
    }

    @Test
    void executeTransfer_shouldSetStatusToFailed_whenAccountServiceReturnsError() {
        UUID transactionId = UUID.randomUUID();
        TransferExecutionRequest request = new TransferExecutionRequest(transactionId);

        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .fromAccountId(UUID.randomUUID())
                .toAccountId(UUID.randomUUID())
                .amount(new BigDecimal("500.00"))
                .status(TransactionStatus.INITIATED)
                .deliveryStatus(DeliveryStatus.SENT)
                .timestamp(Instant.now())
                .build();

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        doThrow(new InvalidTransactionException("Transfer failed: insufficient funds or invalid account"))
                .when(accountServiceClient).executeTransfer(
                        transaction.getFromAccountId(), transaction.getToAccountId(), transaction.getAmount());

        assertThatThrownBy(() -> transactionService.executeTransfer(request))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("Transfer failed");

        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.FAILED);
        verify(transactionRepository).save(transaction);
    }

    // ---- getTransactionHistory() ----

    @Test
    void getTransactionHistory_shouldReturnList_whenTransactionsExist() {
        UUID accountId = UUID.randomUUID();
        Transaction tx = Transaction.builder()
                .transactionId(UUID.randomUUID())
                .fromAccountId(accountId)
                .toAccountId(UUID.randomUUID())
                .amount(new BigDecimal("50.00"))
                .description("Cash deposit")
                .status(TransactionStatus.SUCCESS)
                .deliveryStatus(DeliveryStatus.DELIVERED)
                .timestamp(Instant.now())
                .build();

        when(transactionRepository.findByFromAccountIdOrToAccountIdOrderByTimestampDesc(accountId, accountId))
                .thenReturn(List.of(tx));

        TransactionHistoryResponse historyResponse = TransactionHistoryResponse.builder()
                .transactionId(tx.getTransactionId())
                .fromAccountId(tx.getFromAccountId())
                .toAccountId(tx.getToAccountId())
                .amount(tx.getAmount())
                .description(tx.getDescription())
                .timestamp(tx.getTimestamp())
                .deliveryStatus("DELIVERED")
                .build();
        when(transactionMapper.toHistoryResponse(tx)).thenReturn(historyResponse);

        List<TransactionHistoryResponse> result = transactionService.getTransactionHistory(accountId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDeliveryStatus()).isEqualTo("DELIVERED");
    }

    @Test
    void getTransactionHistory_shouldThrowException_whenNoTransactionsFound() {
        UUID accountId = UUID.randomUUID();
        when(transactionRepository.findByFromAccountIdOrToAccountIdOrderByTimestampDesc(accountId, accountId))
                .thenReturn(List.of());

        assertThatThrownBy(() -> transactionService.getTransactionHistory(accountId))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining("No transactions found");
    }
}
