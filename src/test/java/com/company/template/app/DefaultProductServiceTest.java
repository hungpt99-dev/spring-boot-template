package com.company.template.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.template.app.port.out.ProductEventPublisher;
import com.company.template.app.port.out.ProductRepository;
import com.company.template.app.service.DefaultProductService;
import com.company.template.domain.model.Product;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit conventions: no Spring context, fixed Clock, behavior per use case. */
@ExtendWith(MockitoExtension.class)
class DefaultProductServiceTest {

	private static final Instant NOW = Instant.parse("2026-01-15T10:00:00Z");

	@Mock
	ProductRepository repository;

	@Mock
	ProductEventPublisher events;

	DefaultProductService service;

	@BeforeEach
	void setUp() {
		service = new DefaultProductService(repository, events, Clock.fixed(NOW, ZoneOffset.UTC));
	}

	@Test
	void createAssignsIdTimestampAndPublishesEvent() {
		when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
		Product created = service.create("Widget", "A widget", 999);

		assertThat(created.id()).isNotBlank();
		assertThat(created.name()).isEqualTo("Widget");
		assertThat(created.createdAt()).isEqualTo(NOW);
		verify(events).productCreated(created);
	}

	@Test
	void createRejectsBlankName() {
		assertThatThrownBy(() -> service.create("  ", null, 0)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> service.create("Widget", null, -1)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void getReturnsProductOr404Signal() {
		Product product = new Product("id-1", "Widget", null, 100, NOW);
		when(repository.findById("id-1")).thenReturn(Optional.of(product));

		assertThat(service.get("id-1")).isEqualTo(product);
		assertThatThrownBy(() -> service.get("missing")).isInstanceOf(NoSuchElementException.class);
	}

	@Test
	void listRejectsInvalidPagination() {
		assertThatThrownBy(() -> service.list(-1, 20)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> service.list(0, 0)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> service.list(0, 201)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void listDelegatesToRepository() {
		when(repository.findAll(0, 20)).thenReturn(List.of());
		assertThat(service.list(0, 20)).isEmpty();
	}

}
