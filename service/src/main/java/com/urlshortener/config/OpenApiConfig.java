package com.urlshortener.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI urlShortenerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("URL Shortener API")
                        .description("A production-grade URL shortener service built with Spring Boot 3, " +
                                "DynamoDB (AWS), Redis, and SQS. Supports custom aliases, TTL-based expiry, " +
                                "and click analytics.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("URL Shortener")
                                .url("http://localhost:8080"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT"))
                );
    }
}
