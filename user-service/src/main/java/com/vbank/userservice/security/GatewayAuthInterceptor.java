package com.vbank.userservice.security;

import com.vbank.userservice.exception.UnauthorizedException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * WSO2 API Gateway validates the OAuth2 token forwards the authenticated user's
 * ID via the X-User-Id header on every request it proxies to this
 * service. This service does NOT re-validate the OAuth2 token itself
 * but it DOES defensively check the header is present and well-formed before
 * trusting it, rather than blindly reading it.
 */
@Component
public class GatewayAuthInterceptor implements HandlerInterceptor {

    public static final String USER_ID_HEADER = "X-User-Id";

    public static final String AUTHENTICATED_USER_ID_ATTR = "authenticatedUserId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userIdHeader = request.getHeader(USER_ID_HEADER);

        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new UnauthorizedException("Missing authentication context. Request must originate from the gateway.");
        }

        UUID authenticatedUserId;
        try {
            authenticatedUserId = UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException ex) {
            throw new UnauthorizedException("Invalid authentication context.");
        }

        request.setAttribute(AUTHENTICATED_USER_ID_ATTR, authenticatedUserId);
        return true;
    }
}
