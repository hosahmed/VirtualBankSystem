package com.vbank.accountservice.controller;

import com.vbank.accountservice.dto.request.CreateAccountRequest;
import com.vbank.accountservice.dto.request.TransferRequest;
import com.vbank.accountservice.dto.response.AccountCreatedResponse;
import com.vbank.accountservice.dto.response.AccountDetailsResponse;
import com.vbank.accountservice.dto.response.TransferResponse;
import com.vbank.accountservice.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/accounts")
    public ResponseEntity<AccountCreatedResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountCreatedResponse response = accountService.createAccount(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<AccountDetailsResponse> getAccount(@PathVariable UUID accountId) {
        AccountDetailsResponse response = accountService.getAccount(accountId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}/accounts")
    public ResponseEntity<List<AccountDetailsResponse>> getAccountsByUserId(@PathVariable UUID userId) {
        List<AccountDetailsResponse> response = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/accounts/transfer") // Note: The prompt specifically said PUT /accounts/transfer
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        TransferResponse response = accountService.transfer(request);
        return ResponseEntity.ok(response);
    }
}
