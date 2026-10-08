package com.company.template.common.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Shared event envelope codec. Every published event (direct Kafka or outbox relay) uses
 * this envelope so producers and consumers share one contract:
 * {@code {eventId,type,aggregateType,aggregateId,occurredAt,payload}}. Reuse this — never
 * hand-roll event JSON.
 */
@Component
public class EventEnvelopeCodec {

	private final ObjectMapper mapper;

	public EventEnvelopeCodec(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	public String encode(String type, String aggregateType, String aggregateId, Map<String, ?> payload) {
		return encode(
				new Envelope(UUID.randomUUID().toString(), type, aggregateType, aggregateId, Instant.now(), payload));
	}

	public String encode(Envelope envelope) {
		return mapper.writeValueAsString(envelope);
	}

	public record Envelope(String eventId, String type, String aggregateType, String aggregateId, Instant occurredAt,
			Map<String, ?> payload) {
	}

}
