package com.example.aiworkflowbackend.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for SpringDoc OpenAPI (Swagger UI).
 * This allows customization of the generated API documentation.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Defines a custom OpenAPI bean to provide metadata for the API documentation.
     * @return A customized OpenAPI object.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("AI Workflow Backend API")
                        .description("API for managing AI workflow definitions, including creation, retrieval, updating, and deletion.")
                        .version("1.0.0")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("AI Workflow Backend Documentation")
                        .url("https://example.com/docs")); // Replace with your actual documentation URL
    }
}
