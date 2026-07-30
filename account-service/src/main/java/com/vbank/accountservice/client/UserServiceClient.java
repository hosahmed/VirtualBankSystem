package com.vbank.accountservice.client;

import com.vbank.accountservice.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
@Slf4j
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(@Value("${user-service.url}") String userServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(userServiceUrl)
                .build();
    }

    /**
     * Validates that a user exists by calling GET /users/{userId}/profile.
     * Throws UserNotFoundException if the user is not found.
     */
    public void validateUserExists(UUID userId) {
        try {
            restClient.get()
                    .uri("/users/{userId}/profile", userId)
                    // Add APP-NAME header to bypass interceptor if necessary, or let BFF handle it.
                    // Assuming internal calls bypass Gateway, but interceptor still checks APP-NAME unless disabled for internal IPs.
                    // For now, we'll mimic what was done in AccountServiceClient (which didn't add headers).
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new UserNotFoundException("User not found: " + userId);
                    })
                    .toBodilessEntity();
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validating user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to validate user: " + userId, e);
        }
    }
}
