package com.puntored.transactions_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Puntored Transactions API")
                        .version("1.0.0")
                        .description("API REST para el portal transaccional de recargas móviles de Puntored. " +
                                "Implementa arquitectura hexagonal y permite realizar recargas, consultar " +
                                "proveedores y gestionar historial de transacciones.")
                        .contact(new Contact()
                                .name("Puntored")
                                .email("support@puntored.com")))
                .components(new Components()
                        .addSecuritySchemes("bearer-token",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token Bearer obtenido del endpoint /api/auth")));
    }
}

