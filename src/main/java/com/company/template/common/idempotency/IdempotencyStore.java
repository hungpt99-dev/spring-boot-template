package com.company.template.common.idempotency;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Idempotency-key store SPI. Swap with a Redis/JDBC implementation for multi-instance
 * safety.
 */
public interface IdempotencyStore {

	Optional<String> find(String key);

	void save(String key, String responseBody, int status);

	/**
	 * In-memory implementation: single instance only. Clustered deploys must provide a
	 * shared store.
	 */
	@Component
	@ConditionalOnProperty(prefix = "app.modules", name = "idempotency.enabled", havingValue = "true",
			matchIfMissing = true)
	class InMemoryIdempotencyStore implements IdempotencyStore {

		private record Entry(String body, int status, Instant expiresAt) {
		}

		private final Map<String, Entry> entries = new ConcurrentHashMap<>();

		private final Duration ttl;

		public InMemoryIdempotencyStore(@Value("${app.idempotency.ttl:PT24H}") Duration ttl) {
			this.ttl = ttl;
		}

		@Override
		public Optional<String> find(String key) {
			Entry entry = entries.get(key);
			if (entry == null || Instant.now().isAfter(entry.expiresAt())) {
				entries.remove(key);
				return Optional.empty();
			}
			return Optional.of(entry.body());
		}

		@Override
		public void save(String key, String responseBody, int status) {
			entries.put(key, new Entry(responseBody, status, Instant.now().plus(ttl)));
		}

	}

}
