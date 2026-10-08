package com.company.template.adapter.out.messaging;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.template.common.event.EventEnvelopeCodec;
import com.company.template.domain.model.Product;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/** Unit-proves the event publishers without a broker. */
@ExtendWith(MockitoExtension.class)
class MessagingAdapterTest {

	@Mock
	KafkaTemplate<String, String> kafka;

	private static final ObjectMapper MAPPER = new JsonMapper();

	private static final EventEnvelopeCodec CODEC = new EventEnvelopeCodec(MAPPER);

	private static final Product PRODUCT = new Product("id-1", "Widget", "d", 100,
			Instant.parse("2026-01-15T10:00:00Z"));

	@Test
	@SuppressWarnings("unchecked")
	void kafkaPublisherSendsKeyedRecord() {
		SendResult<String, String> result = Mockito.mock(SendResult.class);
		when(kafka.send(anyString(), anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(result));

		new KafkaProductEventPublisher(kafka, CODEC, "products.events.v1").productCreated(PRODUCT);

		verify(kafka).send(anyString(), anyString(), anyString());
	}

	@Test
	void kafkaPublishFailureIsLoggedNotThrown() {
		CompletableFuture<SendResult<String, String>> failed = new CompletableFuture<>();
		failed.completeExceptionally(new RuntimeException("broker down"));
		when(kafka.send(anyString(), anyString(), anyString())).thenReturn(failed);

		new KafkaProductEventPublisher(kafka, CODEC, "t").productCreated(PRODUCT);
		// No exception escapes: publish failures are logged for retry/DLQ handling.
	}

	@Test
	void noOpPublisherDropsSilently() {
		new NoOpProductEventPublisher().productCreated(PRODUCT);
	}

}
