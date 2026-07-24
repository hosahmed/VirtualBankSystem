package com.vbank.userservice.config;

import com.vbank.userservice.interceptor.AppNameInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AppNameInterceptor appNameInterceptor;

    public WebConfig(AppNameInterceptor appNameInterceptor) {
        this.appNameInterceptor = appNameInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(appNameInterceptor)
                .addPathPatterns("/**"); // Applies to all endpoints
    }
}
