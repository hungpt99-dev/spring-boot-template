package com.company.template.common.cache;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Redis cache backend. Active only when {@code app.modules.redis.enabled=true}. Without
 * it there is simply no {@code CacheManager} (cache abstraction stays dormant); add
 * {@code @Cacheable} annotations only in services that need them.
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(prefix = "app.modules", name = "redis.enabled", havingValue = "true")
public class RedisCacheConfig {

	@Bean
	public CacheManager cacheManager(RedisConnectionFactory connectionFactory,
			@Value("${app.cache.default-ttl:PT10M}") Duration defaultTtl) {
		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(defaultTtl)
			.disableCachingNullValues()
			.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));
		return RedisCacheManager.builder(connectionFactory).cacheDefaults(config).build();
	}

}
