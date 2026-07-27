package com.vbank.loggingservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public OpenAPI loggingServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Logging Service API")
                        .description("Kafka consumer that persists request/response "
                                + "logs from every other microservice into a dump "
                                + "table. The primary interface is the Kafka topic, "
                                + "not HTTP - the endpoints here are a read-only "
                                + "query addition, not the spec's core requirement.")
                        .version("v1")
                        .contact(new Contact()
                                .name("Virtual Bank System Team")));
    }
}
