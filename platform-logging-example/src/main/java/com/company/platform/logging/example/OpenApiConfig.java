// Copyright (c) 2026 Alvin Wilsen Chan Chua
// GitHub: chuaalvw-y
// Licensed under the Alvin Wilsen Chan Chua Proprietary Use-Only License.
// See LICENSE.txt in the project root for full license information.

package com.company.platform.logging.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
class OpenApiConfig {

    @Bean
    OpenAPI ordersOpenApi(@Value("${spring.application.name}") String appName) {
        return new OpenAPI()
                .info(new Info()
                        .title(appName + " API")
                        .description("Example service demonstrating the chua-erp-platform-logging-core library.")
                        .version("1.0.0")
                        .license(new License().name("Apache-2.0")));
    }
}
