package no.autopacker.gatewayapi.config;import org.springframework.cloud.gateway.route.RouteLocator;import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;import org.springframework.context.annotation.Bean;import org.springframework.context.annotation.Configuration;@Configurationpublic class SpringGatewayConfig {    @Bean    public RouteLocator router(RouteLocatorBuilder builder) {        return builder.routes()            // File Delivery API            .route(r -> r.path("/projects/**")                    .and().header("Host", "api.*")                    .uri("http://localhost:8090/projects")                    .id("file-delivery-api"))            // General API            .route(r -> r.path("/api/general/**")                    .and().header("Host", "api.*")                    .uri("http://localhost:8083/api/general")                    .id("general-api"))            .route(r -> r.path("/api/organization/**")                    .and().header("Host", "api.*")                    .uri("http://localhost:8083/api/organization")                    .id("general-api-organization"))            .route(r -> r.path("/api/languages/**")                    .and().header("Host", "api.*")                    .uri("http://localhost:8083/api/languages")                    .id("general-api-languages"))            // Server API            .route(r -> r.path("/api/server/**")                    .and().header("Host", "api.*")                    .uri("http://localhost:8082/api/server")                    .id("server-manager-api"))            // Keycloak            .route(r -> r.path("/auth/**")                    .and().header("Host", "api.*")                    .uri("http://localhost:8080/auth")                    .id("keycloak"))            // User Service API            .route(r -> r.path("/api/auth/**")                    .and().header("Host", "api.*")                    .uri("http://localhost:8081/api/auth")                    .id("userservice-api"))            .route(r -> r.path("/**")                    .and().header("Host", "^(?!(api)).*")                    .uri("http://localhost:8444")                    .id("web-app"))            .build();    }}