package com.vbank.accountservice.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class AppNameInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String appName = request.getHeader("APP-NAME");
        if (appName == null || appName.isEmpty()) {
            log.warn("APP-NAME header is missing. This endpoint should typically be called via the API Gateway.");
        }
        return true;
    }
}
