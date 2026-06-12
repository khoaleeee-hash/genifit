package com.examp.genifit.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {

        String securitySchemeName = "bearerAuth";

        Server railwayServer = new Server();
        railwayServer.setUrl("https://genifit-production.up.railway.app");
        railwayServer.setDescription("Railway Production");

        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Local Development");

        return new OpenAPI()
                .servers(List.of(railwayServer, localServer))
                .info(new Info()
                        .title("GENIFIT API")
                        .version("1.0")
                        .description("Swagger API documentation for GENIFIT")
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        securitySchemeName,
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                )
                .addSecurityItem(
                        new SecurityRequirement().addList(securitySchemeName)
                );
    }
}