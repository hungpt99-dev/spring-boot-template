package com.company.template.common.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * JWT resource-server chain. Active only when {@code app.modules.security.enabled=true}.
 * Requires {@code spring.security.oauth2.resourceserver.jwt.issuer-uri}. Method security
 * ({@code @PreAuthorize}) is enabled alongside.
 */
@Configuration
@EnableMethodSecurity
@ConditionalOnProperty(prefix = "app.modules", name = "security.enabled", havingValue = "true")
public class JwtSecurityConfig {

	private static final String[] PUBLIC = { "/actuator/health/**", "/actuator/info", "/v3/api-docs/**",
			"/swagger-ui/**", "/swagger-ui.html" };

	@Bean
	public SecurityFilterChain jwtChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth.requestMatchers(PUBLIC).permitAll().anyRequest().authenticated())
			.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> {
			}));
		return http.build();
	}

}
