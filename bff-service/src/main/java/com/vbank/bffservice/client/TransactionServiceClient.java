package com.vbank.bffservice.client;

import com.vbank.bffservice.dto.response.TransactionDto;
import com.vbank.bffservice.exception.DownstreamServiceException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * ASSUMED CONTRACT - see AccountServiceClient's note; same caveat.
 */
@Component
public class TransactionServiceClient {

    private final WebClient webClient;

    public TransactionServiceClient(@Qualifier("transactionServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<TransactionDto> getTransactionsForAccount(UUID accountId) {
        return webClient.get()
                .uri("/accounts/{accountId}/transactions", accountId)
                .retrieve()
                .bodyToFlux(TransactionDto.class)
                // Same reasoning as AccountServiceClient: a new account
                // with zero transactions yet is normal, not an error.
                .onErrorResume(WebClientResponseException.NotFound.class, ex -> Flux.empty())
                .onErrorMap(ex -> !(ex instanceof DownstreamServiceException),
                        ex -> new DownstreamServiceException("Call to Transaction Service failed.", ex));
    }
}
