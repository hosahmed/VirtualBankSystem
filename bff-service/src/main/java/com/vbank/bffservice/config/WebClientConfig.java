package com.vbank.bffservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * One WebClient bean per downstream service, each pre-configured with
 * that service's base URL from application.yml. Kept as separate
 * beans (not one generic WebClient + string concatenation at call
 * time) so each client class gets an already-scoped WebClient
 * injected - no risk of a typo building the wrong URL inline in
 * business code.
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient userServiceWebClient(@Value("${services.user-service.base-url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient accountServiceWebClient(@Value("${services.account-service.base-url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient transactionServiceWebClient(@Value("${services.transaction-service.base-url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }
}
