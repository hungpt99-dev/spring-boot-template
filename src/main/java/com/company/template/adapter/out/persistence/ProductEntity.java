package com.company.template.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** JPA entity. Kept separate from the domain model: schema concerns never leak inward. */
@Entity
@Table(name = "products")
public class ProductEntity {

	@Id
	@Column(length = 36)
	private String id;

	@Column(nullable = false, length = 200)
	private String name;

	@Column(length = 2000)
	private String description;

	@Column(name = "price_cents", nullable = false)
	private long priceCents;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected ProductEntity() {
	}

	public ProductEntity(String id, String name, String description, long priceCents, Instant createdAt) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.priceCents = priceCents;
		this.createdAt = createdAt;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public long getPriceCents() {
		return priceCents;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

}
