package com.application.eurder2;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info().title("Eurder API")
                                            .description(
                                                    "Order management API for the Eurder project")
                                            .version("1.0.0"))
                            .externalDocs(new ExternalDocumentation().url(
                                    "https://github.com/nomoli98/2026_Switchfully_eurder"))
                            .components(new Components().addSecuritySchemes("Authorization",
                                                                            new SecurityScheme().type(
                                                                                                        SecurityScheme.Type.APIKEY)
                                                                                                .in(SecurityScheme.In.HEADER)
                                                                                                .name("Authorization")));
    }
}