package com.vbank.transactionservice.client;

import com.vbank.transactionservice.exception.InvalidTransactionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class AccountServiceClient {

    private final RestClient restClient;

    public AccountServiceClient(@Value("${account-service.url}") String accountServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(accountServiceUrl)
                .requestInterceptor((request, body, execution) -> {
                    org.springframework.web.context.request.RequestAttributes attributes = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
                    if (attributes instanceof org.springframework.web.context.request.ServletRequestAttributes) {
                        jakarta.servlet.http.HttpServletRequest servletRequest = ((org.springframework.web.context.request.ServletRequestAttributes) attributes).getRequest();
                        String token = servletRequest.getHeader("X-Auth-Token");
                        if (token != null) {
                            request.getHeaders().add("X-Auth-Token", token);
                        }
                    }
                    return execution.execute(request, body);
                })
                .build();
    }

    /**
     * Retrieves account details to verify ownership.
     */
    public Map<String, Object> getAccount(UUID accountId) {
        try {
            return restClient.get()
                    .uri("/accounts/{accountId}", accountId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new InvalidTransactionException("Account not found: " + accountId);
                    })
                    .body(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});
        } catch (InvalidTransactionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving account {}: {}", accountId, e.getMessage());
            throw new InvalidTransactionException("Failed to retrieve account: " + accountId);
        }
    }

    /**
     * Validates that an account exists by calling GET /accounts/{accountId}/exists.
     * Throws InvalidTransactionException if the account is not found.
     */
    public void validateAccountExists(UUID accountId) {
        try {
            restClient.get()
                    .uri("/accounts/{accountId}/exists", accountId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new InvalidTransactionException("Account not found: " + accountId);
                    })
                    .toBodilessEntity();
        } catch (InvalidTransactionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validating account {}: {}", accountId, e.getMessage());
            throw new InvalidTransactionException("Failed to validate account: " + accountId);
        }
    }

    /**
     * Executes a fund transfer by calling PUT /accounts/transfer on the Account Service.
     * Throws InvalidTransactionException if the Account Service returns an error.
     */
    public void executeTransfer(UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "fromAccountId", fromAccountId.toString(),
                    "toAccountId", toAccountId.toString(),
                    "amount", amount
            );

            restClient.put()
                    .uri("/accounts/transfer")
                    .body(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new InvalidTransactionException("Transfer failed: insufficient funds or invalid account");
                    })
                    .toBodilessEntity();
        } catch (InvalidTransactionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error executing transfer from {} to {}: {}", fromAccountId, toAccountId, e.getMessage());
            throw new InvalidTransactionException("Failed to execute transfer: " + e.getMessage());
        }
    }
}
