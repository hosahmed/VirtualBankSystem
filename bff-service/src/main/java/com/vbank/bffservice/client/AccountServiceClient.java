package com.vbank.bffservice.client;

import com.vbank.bffservice.dto.response.AccountDto;
import com.vbank.bffservice.exception.DownstreamServiceException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Component
public class AccountServiceClient {

    private final WebClient webClient;

    public AccountServiceClient(@Qualifier("accountServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    // 404 means "no accounts for this user yet" — normal, not an error.
    public Flux<AccountDto> getAccountsForUser(UUID userId) {
        return webClient.get()
                .uri("/users/{userId}/accounts", userId)
                .retrieve()
                .bodyToFlux(AccountDto.class)
                .onErrorResume(WebClientResponseException.NotFound.class, ex -> Flux.empty())
                .onErrorMap(ex -> !(ex instanceof DownstreamServiceException),
                        ex -> new DownstreamServiceException("Call to Account Service failed.", ex));
    }
}
