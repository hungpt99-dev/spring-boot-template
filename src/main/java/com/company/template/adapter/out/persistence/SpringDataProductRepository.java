package com.company.template.adapter.out.persistence;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository. Active only when the persistence module is enabled. */
@ConditionalOnProperty(prefix = "app.modules", name = "persistence.enabled", havingValue = "true")
public interface SpringDataProductRepository extends JpaRepository<ProductEntity, String> {

}
