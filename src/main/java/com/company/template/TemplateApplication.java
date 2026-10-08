package com.company.template;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Microservice entry point.
 *
 * <p>
 * Optional capabilities are gated by {@code app.modules.*.enabled} properties. Disabled
 * modules exclude their auto-configurations early via
 * {@code ModularEnvironmentPostProcessor}, so no unused beans or infrastructure
 * connections are created. See {@code application.yml}.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class TemplateApplication {

	public static void main(String[] args) {
		SpringApplication.run(TemplateApplication.class, args);
	}

}
