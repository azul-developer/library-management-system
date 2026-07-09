package com.liz.library.infrastructure.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI libraryApi() {

        return new OpenAPI()
                .info(new Info()
                        .title("Service A: Library API")
                        .description("REST API for the Library Management System")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Lizzeth Sofía Maldonado López")
                                .email("lizzethmaldonadolopez@email.com")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project documentation"));
    }
}