package com.vbank.bffservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bffServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BFF Service API")
                        .description("Backend for Frontend - aggregates User, "
                                + "Account, and Transaction services into "
                                + "frontend-optimized responses. This is the "
                                + "only service that calls other microservices "
                                + "directly; see docs/OPENCODE.md for why.")
                        .version("v1")
                        .contact(new Contact()
                                .name("Virtual Bank System Team")));
    }
}
