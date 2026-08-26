package com.gsb.admission.dossier.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI admissionDossierOpenApi() {
        return new OpenAPI().info(new Info()
                .title("投档来源卷宗系统 API (Admission Evidence Dossier)")
                .version("1.0.0")
                .description("围绕投档事实建立卷宗，汇集多个独立来源核验投档最高分，并按 as_of 封存不可变卷宗。"));
    }
}
