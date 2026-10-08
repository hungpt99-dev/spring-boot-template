package com.company.template.common.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Cross-cutting infrastructure beans with no external dependencies. */
@Configuration
@EnableScheduling // No-op until a @Scheduled bean (e.g. OutboxRelay) is active.
public class CommonConfig {

	/**
	 * Injectable clock: use {@code clock.instant()} instead of {@code Instant.now()} for
	 * testability.
	 */
	@Bean
	public Clock clock() {
		return Clock.systemUTC();
	}

}
