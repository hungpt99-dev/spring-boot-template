package com.company.template.common.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.template.app.port.out.OutboxStore;
import com.company.template.app.port.out.OutboxStore.OutboxMessage;
import com.company.template.common.modules.ModuleProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

/** Relay semantics: publish claimed rows, mark published, count failures. */
@ExtendWith(MockitoExtension.class)
class OutboxRelayTest {

	@Mock
	OutboxStore store;

	@Mock
	KafkaTemplate<String, String> kafka;

	private OutboxRelay relay() {
		return new OutboxRelay(store, kafka, 50, 10, Duration.ofSeconds(10));
	}

	private static OutboxMessage message() {
		return new OutboxMessage(UUID.randomUUID(), "Product", "id-1", "product.created.v1", "products.events.v1",
				"{\"k\":\"v\"}", Instant.now(), null, 0);
	}

	@Test
	@SuppressWarnings("unchecked")
	void publishesClaimedBatchAndMarksPublished() {
		OutboxMessage first = message();
		OutboxMessage second = message();
		when(store.claimBatch(anyInt(), anyInt())).thenReturn(List.of(first, second));
		SendResult<String, String> result = Mockito.mock(SendResult.class);
		when(kafka.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.completedFuture(result));

		relay().relay();

		ArgumentCaptor<ProducerRecord<String, String>> records = ArgumentCaptor.forClass(ProducerRecord.class);
		verify(kafka, Mockito.times(2)).send(records.capture());
		assertThat(records.getAllValues()).extracting(ProducerRecord::topic)
			.containsExactly("products.events.v1", "products.events.v1");
		assertThat(records.getAllValues().get(0).headers().lastHeader("event-id")).isNotNull();
		verify(store).markPublished(first.id());
		verify(store).markPublished(second.id());
	}

	@Test
	void sendFailureMarksFailedAndContinues() {
		OutboxMessage failing = message();
		OutboxMessage succeeding = message();
		when(store.claimBatch(anyInt(), anyInt())).thenReturn(List.of(failing, succeeding));
		CompletableFuture<SendResult<String, String>> failed = new CompletableFuture<>();
		failed.completeExceptionally(new RuntimeException("broker down"));
		SendResult<String, String> ok = Mockito.mock(SendResult.class);
		when(kafka.send(any(ProducerRecord.class))).thenReturn(failed)
			.thenReturn(CompletableFuture.completedFuture(ok));

		relay().relay();

		verify(store).markFailed(any(UUID.class), any(String.class));
		verify(store).markPublished(succeeding.id());
	}

	@Test
	void validatorFailsFastWithoutPersistenceAndKafka() {
		ModuleProperties modules = new ModuleProperties();
		modules.getModules().getOutbox().setEnabled(true);

		assertThatThrownBy(() -> new OutboxModuleValidator(modules).run(null)).isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("requires");
	}

	@Test
	void validatorPassesWhenDependenciesEnabled() throws Exception {
		ModuleProperties modules = new ModuleProperties();
		modules.getModules().getOutbox().setEnabled(true);
		modules.getModules().getPersistence().setEnabled(true);
		modules.getModules().getKafka().setEnabled(true);

		new OutboxModuleValidator(modules).run(null);
	}

}
