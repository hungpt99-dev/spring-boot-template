package com.company.template.adapter.out.persistence;

import com.company.template.app.port.out.OutboxStore;
import com.company.template.app.port.out.OutboxStore.OutboxMessage;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA outbox store. Every method joins the caller's transaction: {@code append} is called
 * inside the aggregate's save transaction (atomic), the relay's {@code claimBatch} runs
 * in its own polling transaction.
 */
@Component
@ConditionalOnProperty(prefix = "app.modules", name = "persistence.enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "app.modules", name = "outbox.enabled", havingValue = "true")
public class JpaOutboxStore implements OutboxStore {

	private final SpringDataOutboxRepository repository;

	public JpaOutboxStore(SpringDataOutboxRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional
	public void append(OutboxMessage message) {
		OutboxEventEntity entity = new OutboxEventEntity();
		entity.setId(message.id());
		entity.setAggregateType(message.aggregateType());
		entity.setAggregateId(message.aggregateId());
		entity.setType(message.type());
		entity.setTopic(message.topic());
		entity.setPayload(message.payload());
		entity.setCreatedAt(message.createdAt());
		entity.setAttempts(0);
		repository.save(entity);
	}

	@Override
	@Transactional
	public List<OutboxMessage> claimBatch(int limit, int maxAttempts) {
		return repository.claim(limit, maxAttempts).stream().map(this::toMessage).toList();
	}

	@Override
	@Transactional
	public void markPublished(UUID id) {
		repository.findById(id).ifPresent(entity -> {
			entity.setPublishedAt(Instant.now());
			entity.setLastError(null);
		});
	}

	@Override
	@Transactional
	public void markFailed(UUID id, String error) {
		repository.findById(id).ifPresent(entity -> {
			entity.setAttempts(entity.getAttempts() + 1);
			entity.setLastError(error != null && error.length() > 2000 ? error.substring(0, 2000) : error);
		});
	}

	private OutboxMessage toMessage(OutboxEventEntity entity) {
		return new OutboxMessage(entity.getId(), entity.getAggregateType(), entity.getAggregateId(), entity.getType(),
				entity.getTopic(), entity.getPayload(), entity.getCreatedAt(), entity.getPublishedAt(),
				entity.getAttempts());
	}

}
