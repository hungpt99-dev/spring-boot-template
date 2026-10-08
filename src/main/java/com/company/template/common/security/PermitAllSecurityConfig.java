package com.company.template.common.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Open chain for local development / services behind a gateway that terminates auth.
 * Active when {@code app.modules.security.enabled=false} (the default). Never enable in
 * production without a compensating control.
 */
@Configuration
@ConditionalOnProperty(prefix = "app.modules", name = "security.enabled", havingValue = "false", matchIfMissing = true)
public class PermitAllSecurityConfig {

	@Bean
	public SecurityFilterChain permitAllChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
		return http.build();
	}

}
