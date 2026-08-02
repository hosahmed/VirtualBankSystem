package com.vbank.accountservice.client;

import com.vbank.accountservice.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Component
@Slf4j
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(@Value("${user-service.url}") String userServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(userServiceUrl)
                .requestInterceptor((request, body, execution) -> {
                    ServletRequestAttributes attrs =
                            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attrs != null) {
                        HttpServletRequest servletRequest = attrs.getRequest();
                        String token = servletRequest.getHeader("X-Auth-Token");
                        if (token != null) {
                            request.getHeaders().set("X-Auth-Token", token);
                        }
                    }
                    return execution.execute(request, body);
                })
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

