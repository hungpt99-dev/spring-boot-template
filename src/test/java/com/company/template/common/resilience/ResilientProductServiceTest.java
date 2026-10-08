package com.company.template.common.resilience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.company.template.app.port.in.ProductService;
import com.company.template.domain.model.Product;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** The resilience decorator adds policy, not logic: prove pure delegation. */
@ExtendWith(MockitoExtension.class)
class ResilientProductServiceTest {

	@Mock
	ProductService delegate;

	private static final Product PRODUCT = new Product("id-1", "Widget", "d", 100,
			Instant.parse("2026-01-15T10:00:00Z"));

	@Test
	void delegatesAllUseCases() {
		when(delegate.create(anyString(), any(), anyLong())).thenReturn(PRODUCT);
		when(delegate.get("id-1")).thenReturn(PRODUCT);
		when(delegate.list(anyInt(), anyInt())).thenReturn(List.of(PRODUCT));
		ResilientProductService service = new ResilientProductService(delegate);

		assertThat(service.create("Widget", "d", 100)).isEqualTo(PRODUCT);
		assertThat(service.get("id-1")).isEqualTo(PRODUCT);
		assertThat(service.list(0, 20)).containsExactly(PRODUCT);
	}

}
