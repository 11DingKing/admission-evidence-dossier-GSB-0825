package com.dossier.admission.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI admissionDossierOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("投档来源卷宗系统 API")
                        .version("1.0.0")
                        .description("围绕 fact_key 核验多来源投档数据，封存不可变卷宗"));
    }
}
