package com.vbank.bffservice.client;

import com.vbank.bffservice.dto.response.AccountDto;
import com.vbank.bffservice.exception.DownstreamServiceException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * ASSUMED CONTRACT - endpoint path and response shape follow the
 * spec's documented GET /users/{userId}/accounts. Not yet verified
 * against the real Account Service implementation - reconcile once
 * that code is available (see bff-service/README.md).
 */
@Component
public class AccountServiceClient {

    private final WebClient webClient;

    public AccountServiceClient(@Qualifier("accountServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<AccountDto> getAccountsForUser(UUID userId) {
        return webClient.get()
                .uri("/users/{userId}/accounts", userId)
                .retrieve()
                .bodyToFlux(AccountDto.class)
                // Per spec, Account Service returns 404 when a user has
                // zero accounts - that's a normal case (e.g. a
                // brand-new user right after registration), not a
                // failure, so we treat it as an empty list rather than
                // propagating an error for the whole dashboard.
                .onErrorResume(WebClientResponseException.NotFound.class, ex -> Flux.empty())
                .onErrorMap(ex -> !(ex instanceof DownstreamServiceException),
                        ex -> new DownstreamServiceException("Call to Account Service failed.", ex));
    }
}
