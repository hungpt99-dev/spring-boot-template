package com.company.template.common.modules;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Single knob for optional capabilities. Each flag defaults to the lightweight choice;
 * enabling a flag activates that module's beans (and only those).
 *
 * <p>
 * Conventions: property {@code app.modules.<name>.enabled}, default documented in
 * {@code application.yml} and {@code additional-spring-configuration-metadata.json}.
 */
@ConfigurationProperties(prefix = "app")
public class ModuleProperties {

	private final Modules modules = new Modules();

	public Modules getModules() {
		return modules;
	}

	public static class Modules {

		/** JPA + Flyway + PostgreSQL. Default false: in-memory repository is used. */
		private final Module persistence = new Module();

		/** Redis cache/session backend. Default false: no cache provider. */
		private final Module redis = new Module();

		/** Kafka producer. Default false: no-op event publisher is used. */
		private final Module kafka = new Module();

		/** OAuth2 JWT resource server. Default false: all endpoints permit-all. */
		private final Module security = new Module();

		/** Resilience4j decorators. Default false: direct delegation. */
		private final Module resilience = new Module();

		/**
		 * Transactional outbox (reliable event publishing). Default false. Requires
		 * {@code persistence} + {@code kafka}; validated at startup.
		 */
		private final Module outbox = new Module();

		/** Idempotency-Key handling. Default true: in-memory store, no infra needed. */
		private final Module idempotency = new Module(true);

		/** OpenAPI/Swagger UI. Default true: read-only docs endpoints. */
		private final Module docs = new Module(true);

		public Module getPersistence() {
			return persistence;
		}

		public Module getRedis() {
			return redis;
		}

		public Module getKafka() {
			return kafka;
		}

		public Module getSecurity() {
			return security;
		}

		public Module getResilience() {
			return resilience;
		}

		public Module getOutbox() {
			return outbox;
		}

		public Module getIdempotency() {
			return idempotency;
		}

		public Module getDocs() {
			return docs;
		}

	}

	public static class Module {

		private boolean enabled;

		public Module() {
		}

		public Module(boolean enabled) {
			this.enabled = enabled;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

	}

}
