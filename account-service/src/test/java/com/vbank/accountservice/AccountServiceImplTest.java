package com.vbank.accountservice;

import com.vbank.accountservice.dto.request.CreateAccountRequest;
import com.vbank.accountservice.dto.request.TransferRequest;
import com.vbank.accountservice.dto.response.AccountCreatedResponse;
import com.vbank.accountservice.dto.response.AccountDetailsResponse;
import com.vbank.accountservice.dto.response.TransferResponse;
import com.vbank.accountservice.entity.Account;
import com.vbank.accountservice.entity.AccountStatus;
import com.vbank.accountservice.entity.AccountType;
import com.vbank.accountservice.exception.AccountNotFoundException;
import com.vbank.accountservice.exception.InsufficientFundsException;
import com.vbank.accountservice.exception.InvalidAccountRequestException;
import com.vbank.accountservice.client.UserServiceClient;
import com.vbank.accountservice.mapper.AccountMapper;
import com.vbank.accountservice.repository.AccountRepository;
import com.vbank.accountservice.service.impl.AccountServiceImpl;
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
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private UserServiceClient userServiceClient;

    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        accountService = new AccountServiceImpl(accountRepository, accountMapper, userServiceClient);
    }

    @Test
    void createAccount_shouldSaveAccount_whenValidRequest() {
        UUID userId = UUID.randomUUID();
        CreateAccountRequest request = new CreateAccountRequest(userId, "SAVINGS", new BigDecimal("100.00"));

        Account savedAccount = Account.builder()
                .accountId(UUID.randomUUID())
                .userId(userId)
                .accountNumber("1234567890")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("100.00"))
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        AccountCreatedResponse expectedResponse = AccountCreatedResponse.builder()
                .accountId(savedAccount.getAccountId())
                .accountNumber("1234567890")
                .message("Account created successfully.")
                .build();

        when(accountMapper.toCreatedResponse(savedAccount)).thenReturn(expectedResponse);

        AccountCreatedResponse response = accountService.createAccount(request);

        assertThat(response.getAccountNumber()).isEqualTo("1234567890");
        assertThat(response.getMessage()).isEqualTo("Account created successfully.");
    }

    @Test
    void createAccount_shouldThrowException_whenInvalidAccountType() {
        UUID userId = UUID.randomUUID();
        CreateAccountRequest request = new CreateAccountRequest(userId, "INVALID", new BigDecimal("100.00"));

        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(InvalidAccountRequestException.class)
                .hasMessageContaining("Invalid account type");
    }

    @Test
    void getAccount_shouldReturnDetails_whenAccountExists() {
        UUID accountId = UUID.randomUUID();
        Account account = Account.builder()
                .accountId(accountId)
                .accountNumber("1234567890")
                .accountType(AccountType.CHECKING)
                .balance(new BigDecimal("500.00"))
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        AccountDetailsResponse expectedResponse = AccountDetailsResponse.builder()
                .accountId(accountId)
                .accountNumber("1234567890")
                .accountType("CHECKING")
                .balance(new BigDecimal("500.00"))
                .status("ACTIVE")
                .build();

        when(accountMapper.toDetailsResponse(account)).thenReturn(expectedResponse);

        AccountDetailsResponse response = accountService.getAccount(accountId);

        assertThat(response.getAccountNumber()).isEqualTo("1234567890");
        assertThat(response.getBalance()).isEqualTo(new BigDecimal("500.00"));
    }

    @Test
    void getAccount_shouldThrowException_whenAccountNotFound() {
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount(accountId))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Account not found");
    }

    @Test
    void getAccountsByUserId_shouldReturnList_whenAccountsExist() {
        UUID userId = UUID.randomUUID();
        Account account = Account.builder().accountId(UUID.randomUUID()).build();
        when(accountRepository.findByUserId(userId)).thenReturn(List.of(account));

        AccountDetailsResponse details = AccountDetailsResponse.builder().build();
        when(accountMapper.toDetailsResponse(account)).thenReturn(details);

        List<AccountDetailsResponse> response = accountService.getAccountsByUserId(userId);

        assertThat(response).hasSize(1);
    }

    @Test
    void getAccountsByUserId_shouldThrowException_whenNoAccountsFound() {
        UUID userId = UUID.randomUUID();
        when(accountRepository.findByUserId(userId)).thenReturn(List.of());

        assertThatThrownBy(() -> accountService.getAccountsByUserId(userId))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("No accounts found");
    }

    @Test
    void transfer_shouldCompleteSuccessfully_whenValidRequest() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();
        TransferRequest request = new TransferRequest(fromId, toId, new BigDecimal("50.00"));

        Account fromAccount = Account.builder()
                .accountId(fromId)
                .balance(new BigDecimal("100.00"))
                .status(AccountStatus.ACTIVE)
                .build();

        Account toAccount = Account.builder()
                .accountId(toId)
                .balance(new BigDecimal("100.00"))
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountRepository.findById(fromId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toId)).thenReturn(Optional.of(toAccount));

        TransferResponse response = accountService.transfer(request);

        assertThat(response.getMessage()).isEqualTo("Account updated successfully.");
        assertThat(fromAccount.getBalance()).isEqualTo(new BigDecimal("50.00"));
        assertThat(toAccount.getBalance()).isEqualTo(new BigDecimal("150.00"));
        
        verify(accountRepository).save(fromAccount);
        verify(accountRepository).save(toAccount);
    }

    @Test
    void transfer_shouldThrowException_whenSameAccount() {
        UUID id = UUID.randomUUID();
        TransferRequest request = new TransferRequest(id, id, new BigDecimal("50.00"));

        assertThatThrownBy(() -> accountService.transfer(request))
                .isInstanceOf(InvalidAccountRequestException.class)
                .hasMessageContaining("Cannot transfer to the same account");
    }

    @Test
    void transfer_shouldThrowException_whenInsufficientFunds() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();
        TransferRequest request = new TransferRequest(fromId, toId, new BigDecimal("500.00"));

        Account fromAccount = Account.builder()
                .accountId(fromId)
                .balance(new BigDecimal("100.00"))
                .status(AccountStatus.ACTIVE)
                .build();

        Account toAccount = Account.builder()
                .accountId(toId)
                .balance(new BigDecimal("100.00"))
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountRepository.findById(fromId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toId)).thenReturn(Optional.of(toAccount));

        assertThatThrownBy(() -> accountService.transfer(request))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("Insufficient funds");
    }
}
