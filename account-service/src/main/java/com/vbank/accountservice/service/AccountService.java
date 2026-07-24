package com.vbank.accountservice.service;

import com.vbank.accountservice.dto.request.CreateAccountRequest;
import com.vbank.accountservice.dto.request.TransferRequest;
import com.vbank.accountservice.dto.response.AccountCreatedResponse;
import com.vbank.accountservice.dto.response.AccountDetailsResponse;
import com.vbank.accountservice.dto.response.TransferResponse;

import java.util.List;
import java.util.UUID;

public interface AccountService {
    AccountCreatedResponse createAccount(CreateAccountRequest request);
    AccountDetailsResponse getAccount(UUID accountId);
    List<AccountDetailsResponse> getAccountsByUserId(UUID userId);
    TransferResponse transfer(TransferRequest request);
}
