package com.example.demo.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI createOpenAPI() {

        Info apiInfo = new Info()
                .title("Dket Back-End API")
                .description("Dket 백엔드 API 명세서")
                .version("1.0.0");
        String jwtSchemeName = "JWT_TOKEN";
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(jwtSchemeName);
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName,
                        new SecurityScheme()
                                .name(jwtSchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                .addRequestBodies("multipartRequestBody",
                    new RequestBody()
                            .content(new Content().addMediaType("multipart/form-data",
                                    new MediaType().schema(new Schema<>().type("object")))));
        return new OpenAPI()
                .addServersItem(new Server().url("/"))
                .info(apiInfo)
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}