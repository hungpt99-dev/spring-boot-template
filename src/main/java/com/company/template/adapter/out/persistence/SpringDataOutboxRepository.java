package com.company.template.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Outbox rows. Active only when persistence + outbox modules are enabled. */
@ConditionalOnProperty(prefix = "app.modules", name = "persistence.enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "app.modules", name = "outbox.enabled", havingValue = "true")
public interface SpringDataOutboxRepository extends JpaRepository<OutboxEventEntity, UUID> {

	/**
	 * Claims unpublished rows, oldest first. {@code FOR UPDATE SKIP LOCKED} makes
	 * concurrent relay instances (multi-replica) safe.
	 */
	@Query(value = "SELECT * FROM outbox_events WHERE published_at IS NULL AND attempts < :maxAttempts"
			+ " ORDER BY created_at LIMIT :limit FOR UPDATE SKIP LOCKED", nativeQuery = true)
	List<OutboxEventEntity> claim(@Param("limit") int limit, @Param("maxAttempts") int maxAttempts);

}
