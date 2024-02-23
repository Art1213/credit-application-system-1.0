package me.dio.credit.application.system.configuration

import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Swagger3Config {
    @Bean
    fun publicApi(): GroupedOpenApi? {
        return GroupedOpenApi.builder()
            .group("springcreditapplicationsystem-public")
            .pathsToMatch("/api/customers/**", "/api/credits/**")
            .addOpenApiCustomizer { openApi ->
                openApi.info.title = "API de Sistema de Analise de Solicitação de Crédito"
                openApi.info.version = "1.0.0"
                openApi.info.description = "Um sistema para analisar a solicitação de créditos."
            }
            .addOpenApiCustomizer { openApi ->
                openApi.components.securitySchemes["ApiKey"] = SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .`in`(SecurityScheme.In.HEADER)
                    .name("X-API-Key")
            }
            .packagesToScan("me.dio.credit.application.system.controller")
            .build()
    }
}