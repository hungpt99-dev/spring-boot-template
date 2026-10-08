package com.company.template.common.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Excludes auto-configurations of disabled modules BEFORE auto-configuration runs, so
 * disabled features create no beans and need no infrastructure.
 *
 * <p>
 * Runs on plain {@code app.modules.*.enabled} properties (system props, env vars, yaml
 * all work). Merges with any user-provided {@code spring.autoconfigure.exclude}.
 */
public class ModularEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

	static final String EXCLUDE_KEY = "spring.autoconfigure.exclude";

	// Spring Boot 4 repackaged auto-configurations out of
	// org.springframework.boot.autoconfigure.* into per-module packages.
	private static final List<String> PERSISTENCE_EXCLUDES = List.of(
			"org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
			"org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration",
			"org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration");

	private static final List<String> REDIS_EXCLUDES = List
		.of("org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration");

	private static final List<String> KAFKA_EXCLUDES = List
		.of("org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration");

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		List<String> excludes = new ArrayList<>(existingExcludes(environment));
		if (!enabled(environment, "persistence")) {
			excludes.addAll(PERSISTENCE_EXCLUDES);
		}
		if (!enabled(environment, "redis")) {
			excludes.addAll(REDIS_EXCLUDES);
		}
		if (!enabled(environment, "kafka")) {
			excludes.addAll(KAFKA_EXCLUDES);
		}
		Map<String, Object> source = new HashMap<>();
		source.put(EXCLUDE_KEY, String.join(",", excludes.stream().distinct().toList()));
		// First so the merged value wins over any single later definition.
		environment.getPropertySources().addFirst(new MapPropertySource("templateModules", source));
	}

	private boolean enabled(ConfigurableEnvironment environment, String module) {
		return environment.getProperty("app.modules." + module + ".enabled", Boolean.class, false);
	}

	private List<String> existingExcludes(ConfigurableEnvironment environment) {
		String existing = environment.getProperty(EXCLUDE_KEY, String.class, "");
		if (existing.isBlank()) {
			return List.of();
		}
		return Arrays.stream(existing.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 100;
	}

}
