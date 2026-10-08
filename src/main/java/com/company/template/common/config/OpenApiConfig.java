package com.company.template.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Minimal OpenAPI metadata. Endpoint exposure itself is bridged from app.modules.docs in
 * yaml.
 */
@Configuration
@ConditionalOnClass(OpenAPI.class)
@ConditionalOnProperty(prefix = "app.modules", name = "docs.enabled", havingValue = "true", matchIfMissing = true)
public class OpenApiConfig {

	@Bean
	public OpenAPI api(@Value("${spring.application.name:microservice}") String name,
			@Value("${app.api.version:v1}") String version) {
		return new OpenAPI().info(new Info().title(name + " API")
			.version(version)
			.description("Company-standard microservice API. Error model: RFC 9457 Problem Details."));
	}

}
