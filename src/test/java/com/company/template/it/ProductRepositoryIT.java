package com.company.template.it;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.template.app.port.out.ProductRepository;
import com.company.template.domain.model.Product;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Persistence-module IT. Needs Docker; skipped automatically without it. Pattern for all
 * infra tests: Testcontainers + {@code @ServiceConnection}.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = "app.modules.persistence.enabled=true")
class ProductRepositoryIT {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));

	@Autowired
	ProductRepository repository;

	@Test
	void saveAndFindRoundTrip() {
		Product saved = repository.save(new Product("it-1", "IT Widget", "it", 500, Instant.now()));

		assertThat(repository.findById(saved.id())).isPresent();
		assertThat(repository.findAll(0, 20)).extracting(Product::id).contains(saved.id());
	}

}
