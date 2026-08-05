package com.example.hsbcproject.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI portfolioManagerOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Portfolio Manager API")
                .description("REST API for managing a simple financial portfolio")
                .version("v1")
                .contact(new Contact().name("HSBC Training Team"))
                .license(new License().name("Internal Training Use")));
    }
}

