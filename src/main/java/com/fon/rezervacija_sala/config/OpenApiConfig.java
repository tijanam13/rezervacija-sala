package com.fon.rezervacija_sala.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SEMA_JWT = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistem za rezervaciju sala - API")
                        .description("REST API za rezervaciju sala na fakultetu")
                        .version("1.0")
                        .contact(new Contact().name("Tijana")))
                .addSecurityItem(new SecurityRequirement().addList(SEMA_JWT))
                .components(new Components()
                        .addSecuritySchemes(SEMA_JWT, new SecurityScheme()
                                .name(SEMA_JWT)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Nalepi JWT token dobijen sa POST /api/auth/login.")));
    }

}
