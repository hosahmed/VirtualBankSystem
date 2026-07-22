package com.vbank.bffservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "BFF Service API",
                description = "Aggregates User, Account, and Transaction services "
                        + "into frontend-optimized responses. The only service "
                        + "that calls other microservices directly.",
                version = "1.0.0"
        )
)
public class OpenApiConfig {
}
