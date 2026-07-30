package com.vbank.bffservice.client;

import com.vbank.bffservice.dto.response.UserProfileDto;
import com.vbank.bffservice.exception.DownstreamServiceException;
import com.vbank.bffservice.exception.UpstreamUserNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(@Qualifier("userServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<UserProfileDto> getProfile(UUID userId) {
        return webClient.get()
                .uri("/users/{userId}/profile", userId)
                .header("X-User-Id", userId.toString())
                .retrieve()
                .bodyToMono(UserProfileDto.class)
                .onErrorMap(this::translateError);
    }

    private Throwable translateError(Throwable ex) {
        if (ex instanceof WebClientResponseException webEx
                && webEx.getStatusCode().value() == 404) {
            return new UpstreamUserNotFoundException("User with ID not found in User Service.");
        }
        return new DownstreamServiceException("Call to User Service failed.", ex);
    }
}
