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

/**
 * IMPORTANT: this client does NOT call User Service's /login or
 * /register - only the read-only /profile endpoint the dashboard
 * needs. A BFF client class should only expose what its actual
 * callers need, not mirror a downstream service's entire API surface -
 * that keeps this class's purpose obvious at a glance.
 */
@Component
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(@Qualifier("userServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<UserProfileDto> getProfile(UUID userId) {
        // User Service's GatewayAuthInterceptor requires X-User-Id to
        // be present AND to match the requested {userId} path variable
        // (see UserController.getProfile's ownership check). The BFF
        // sits on the trusted side of that boundary - same as the
        // gateway - so it must set this header itself when calling on
        // a user's behalf, forwarding the same ID being requested.
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
