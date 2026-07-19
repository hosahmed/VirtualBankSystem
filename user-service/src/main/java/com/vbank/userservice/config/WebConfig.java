package com.vbank.userservice.config;

import com.vbank.userservice.security.GatewayAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Register the interceptor ONLY on the profile endpoint.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final GatewayAuthInterceptor gatewayAuthInterceptor;

    public WebConfig(GatewayAuthInterceptor gatewayAuthInterceptor) {
        this.gatewayAuthInterceptor = gatewayAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(gatewayAuthInterceptor)
                .addPathPatterns("/users/*/profile");
    }
}
