package com.vbank.accountservice.controller;

import com.vbank.accountservice.dto.request.CreateAccountRequest;
import com.vbank.accountservice.dto.request.TransferRequest;
import com.vbank.accountservice.dto.response.AccountCreatedResponse;
import com.vbank.accountservice.dto.response.AccountDetailsResponse;
import com.vbank.accountservice.dto.response.TransferResponse;
import com.vbank.accountservice.exception.ErrorResponse;
import com.vbank.accountservice.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Account Service", description = "Manage bank accounts — create, retrieve, transfer")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/accounts")
    @Operation(summary = "Create a new bank account")
    @ApiResponse(responseCode = "201", description = "Account created",
            content = @Content(schema = @Schema(implementation = AccountCreatedResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid account type or initial balance",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<AccountCreatedResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountCreatedResponse response = accountService.createAccount(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/accounts/{accountId}")
    @Operation(summary = "Get account details by ID")
    @ApiResponse(responseCode = "200", description = "Account details",
            content = @Content(schema = @Schema(implementation = AccountDetailsResponse.class)))
    @ApiResponse(responseCode = "404", description = "Account not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<AccountDetailsResponse> getAccount(@PathVariable UUID accountId) {
        AccountDetailsResponse response = accountService.getAccount(accountId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}/accounts")
    @Operation(summary = "List all accounts for a user")
    @ApiResponse(responseCode = "200", description = "List of accounts")
    @ApiResponse(responseCode = "404", description = "No accounts found for user",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<List<AccountDetailsResponse>> getAccountsByUserId(@PathVariable UUID userId) {
        List<AccountDetailsResponse> response = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/accounts/transfer")
    @Operation(summary = "Transfer funds between accounts")
    @ApiResponse(responseCode = "200", description = "Transfer completed",
            content = @Content(schema = @Schema(implementation = TransferResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid accounts or insufficient funds",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        TransferResponse response = accountService.transfer(request);
        return ResponseEntity.ok(response);
    }
}
