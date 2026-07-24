package com.vbank.userservice.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AppNameInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AppNameInterceptor.class);

    private static final String APP_NAME_HEADER = "APP-NAME";
    private static final String PORTAL = "PORTAL";
    private static final String MOBILE = "MOBILE";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Bypass validation for Swagger docs
        String uri = request.getRequestURI();
        if (uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui")) {
            return true;
        }

        String appName = request.getHeader(APP_NAME_HEADER);

        if (appName == null || appName.isBlank()) {
            log.warn("APP-NAME header is missing for request: {} {}", request.getMethod(), uri);
        } else if (!appName.equals(PORTAL) && !appName.equals(MOBILE)) {
            log.warn("Invalid APP-NAME header value '{}' for request: {} {}", appName, request.getMethod(), uri);
        } else {
            log.debug("APP-NAME header: {}", appName);
        }

        return true;
    }
}
