package hhplus.concert.reservation.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("headerAuth", securityScheme()))
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("Concert Ticketing API") // API의 제목
                .description("콘서트 티켓팅 애플리케이션을 위한 API 문서입니다.") // API에 대한 설명
                .version("1.0.0"); // API의 버전
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER);
    }
}
