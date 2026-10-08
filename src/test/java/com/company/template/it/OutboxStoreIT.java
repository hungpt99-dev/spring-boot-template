package com.company.template.it;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.template.adapter.out.persistence.ProductPersistenceAdapter;
import com.company.template.adapter.out.persistence.SpringDataOutboxRepository;
import com.company.template.app.port.out.OutboxStore;
import com.company.template.domain.model.Product;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Persistence + outbox IT (Flyway V1+V2 migrate on the container). Needs Docker; skipped
 * automatically without it.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = { "app.modules.persistence.enabled=true", "app.modules.outbox.enabled=true",
		"app.modules.kafka.enabled=true", "spring.kafka.bootstrap-servers=localhost:1" })
@Transactional
class OutboxStoreIT {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));

	@Autowired
	ProductPersistenceAdapter products;

	@Autowired
	OutboxStore outbox;

	@Autowired
	SpringDataOutboxRepository rows;

	@Test
	void saveWritesAggregateAndOutboxRowAtomically() {
		Product saved = products.save(new Product(UUID.randomUUID().toString(), "IT", null, 100, Instant.now()));

		assertThat(rows.findAll()).anySatisfy(row -> {
			assertThat(row.getAggregateId()).isEqualTo(saved.id());
			assertThat(row.getType()).isEqualTo("product.created.v1");
			assertThat(row.getPublishedAt()).isNull();
		});
		assertThat(outbox.claimBatch(50, 10)).isNotEmpty();
	}

	@Test
	void claimPublishCycleCompletes() {
		outbox.append(new OutboxStore.OutboxMessage(UUID.randomUUID(), "Product", "agg-1", "product.created.v1", "t",
				"{}", Instant.now(), null, 0));

		var claimed = outbox.claimBatch(50, 10);
		assertThat(claimed).hasSize(1);
		outbox.markPublished(claimed.get(0).id());

		assertThat(outbox.claimBatch(50, 10)).isEmpty();
	}

}
