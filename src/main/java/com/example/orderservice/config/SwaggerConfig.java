package com.example.orderservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order service")
                        .version("1.0")
                        .description("API для работы с заказами с JWT аутентификацией"))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .schemaRequirement("BearerAuth", new SecurityScheme()
                        .name("Authorization")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .displayName("Admin API")
                .pathsToMatch("/api/auth/**", "/api/users/**", "/api/orders/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("user")
                .displayName("User API")
                .pathsToMatch("/api/auth/**", "/api/orders/**")
                .addOpenApiMethodFilter(method ->
                        !method.getName().equals("changeOrderStatus")
                                && !method.getName().equals("getAllOrders"))
                .build();
    }
}