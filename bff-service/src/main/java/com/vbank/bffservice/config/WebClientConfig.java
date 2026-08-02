package com.vbank.bffservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private org.springframework.web.reactive.function.client.ExchangeFilterFunction forwardAuthToken() {
        return (request, next) -> {
            org.springframework.web.context.request.RequestAttributes attributes = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attributes instanceof org.springframework.web.context.request.ServletRequestAttributes) {
                jakarta.servlet.http.HttpServletRequest servletRequest = ((org.springframework.web.context.request.ServletRequestAttributes) attributes).getRequest();
                String token = servletRequest.getHeader("X-Auth-Token");
                if (token != null) {
                    org.springframework.web.reactive.function.client.ClientRequest newRequest = org.springframework.web.reactive.function.client.ClientRequest.from(request)
                            .header("X-Auth-Token", token)
                            .build();
                    return next.exchange(newRequest);
                }
            }
            return next.exchange(request);
        };
    }

    @Bean
    public WebClient userServiceWebClient(@Value("${services.user-service.base-url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).filter(forwardAuthToken()).build();
    }

    @Bean
    public WebClient accountServiceWebClient(@Value("${services.account-service.base-url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).filter(forwardAuthToken()).build();
    }

    @Bean
    public WebClient transactionServiceWebClient(@Value("${services.transaction-service.base-url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).filter(forwardAuthToken()).build();
    }
}
