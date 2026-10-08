package com.company.template.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.template.common.modules.ModuleProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

/** Proves module-flag defaults and binding (the template's single control knob). */
class ModulePropertiesTest {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
		.withUserConfiguration(PropsConfig.class);

	@Test
	void lightweightDefaults() {
		runner.run(context -> {
			ModuleProperties.Modules modules = context.getBean(ModuleProperties.class).getModules();
			assertThat(modules.getPersistence().isEnabled()).isFalse();
			assertThat(modules.getRedis().isEnabled()).isFalse();
			assertThat(modules.getKafka().isEnabled()).isFalse();
			assertThat(modules.getSecurity().isEnabled()).isFalse();
			assertThat(modules.getResilience().isEnabled()).isFalse();
			assertThat(modules.getIdempotency().isEnabled()).isTrue();
			assertThat(modules.getDocs().isEnabled()).isTrue();
		});
	}

	@Test
	void flagsBindFromProperties() {
		runner
			.withPropertyValues("app.modules.persistence.enabled=true", "app.modules.security.enabled=true",
					"app.modules.idempotency.enabled=false", "app.idempotency.ttl=1h")
			.run(context -> {
				ModuleProperties.Modules modules = context.getBean(ModuleProperties.class).getModules();
				assertThat(modules.getPersistence().isEnabled()).isTrue();
				assertThat(modules.getSecurity().isEnabled()).isTrue();
				assertThat(modules.getIdempotency().isEnabled()).isFalse();
			});
	}

	@Configuration
	@EnableConfigurationProperties(ModuleProperties.class)
	static class PropsConfig {

	}

}
