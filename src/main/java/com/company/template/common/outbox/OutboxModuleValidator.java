package com.company.template.common.outbox;

import com.company.template.common.modules.ModuleProperties;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Fail-fast guard: the outbox is only valid with persistence (atomicity) and Kafka
 * (delivery). A clear startup error beats silently piling up rows.
 */
@Component
@ConditionalOnProperty(prefix = "app.modules", name = "outbox.enabled", havingValue = "true")
public class OutboxModuleValidator implements ApplicationRunner {

	private final ModuleProperties modules;

	public OutboxModuleValidator(ModuleProperties modules) {
		this.modules = modules;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!modules.getModules().getPersistence().isEnabled() || !modules.getModules().getKafka().isEnabled()) {
			throw new IllegalStateException("app.modules.outbox requires app.modules.persistence.enabled=true"
					+ " and app.modules.kafka.enabled=true");
		}
	}

}
