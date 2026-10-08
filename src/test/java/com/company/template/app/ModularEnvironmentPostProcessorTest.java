package com.company.template.common.modules;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

/** Unit-proves the module gate: disabled modules add excludes, enabled modules do not. */
class ModularEnvironmentPostProcessorTest {

	private final ModularEnvironmentPostProcessor processor = new ModularEnvironmentPostProcessor();

	@Test
	void disabledModulesExcludeTheirAutoConfigurations() {
		MockEnvironment env = new MockEnvironment();

		processor.postProcessEnvironment(env, new SpringApplication());

		String excludes = env.getProperty(ModularEnvironmentPostProcessor.EXCLUDE_KEY);
		assertThat(excludes).contains("jdbc.autoconfigure.DataSourceAutoConfiguration");
		assertThat(excludes).contains("hibernate.autoconfigure.HibernateJpaAutoConfiguration");
		assertThat(excludes).contains("flyway.autoconfigure.FlywayAutoConfiguration");
		assertThat(excludes).contains("data.redis.autoconfigure.DataRedisAutoConfiguration");
		assertThat(excludes).contains("kafka.autoconfigure.KafkaAutoConfiguration");
	}

	@Test
	void enabledModulesKeepTheirAutoConfigurations() {
		MockEnvironment env = new MockEnvironment().withProperty("app.modules.persistence.enabled", "true")
			.withProperty("app.modules.redis.enabled", "true")
			.withProperty("app.modules.kafka.enabled", "true");

		processor.postProcessEnvironment(env, new SpringApplication());

		String excludes = env.getProperty(ModularEnvironmentPostProcessor.EXCLUDE_KEY);
		assertThat(excludes == null || excludes.isBlank()).isTrue();
	}

	@Test
	void mergesWithUserProvidedExcludes() {
		MockEnvironment env = new MockEnvironment().withProperty(ModularEnvironmentPostProcessor.EXCLUDE_KEY,
				"com.example.Custom");

		processor.postProcessEnvironment(env, new SpringApplication());

		String excludes = env.getProperty(ModularEnvironmentPostProcessor.EXCLUDE_KEY);
		assertThat(excludes).contains("com.example.Custom");
		assertThat(excludes).contains("jdbc.autoconfigure.DataSourceAutoConfiguration");
	}

}
