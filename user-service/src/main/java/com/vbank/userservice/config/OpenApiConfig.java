package com.vbank.userservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
        info = @Info(
                title = "User Service API",
                description = "Manages user authentication, registration, and profile information. Part of the Virtual Bank System.",
                version = "1.0.0"
        )
)
@SecurityScheme(
        name = "X-User-Id",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "X-User-Id",
        description = "Authenticated user ID forwarded by WSO2 API Gateway. Required for the profile endpoint."
)
public class OpenApiConfig {
}
