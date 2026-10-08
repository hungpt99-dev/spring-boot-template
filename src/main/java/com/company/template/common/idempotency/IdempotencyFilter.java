package com.company.template.common.idempotency;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * Idempotency conventions: clients send {@code Idempotency-Key} on unsafe methods
 * (POST/PUT/PATCH/DELETE). A repeated key replays the stored response instead of
 * re-executing the handler. Active when {@code app.modules.idempotency.enabled=true}
 * (default).
 *
 * <p>
 * Scope: single instance with the default in-memory store. Keys are scoped per
 * authenticated principal when security is enabled (header fallback).
 */
@Component
@ConditionalOnProperty(prefix = "app.modules", name = "idempotency.enabled", havingValue = "true",
		matchIfMissing = true)
public class IdempotencyFilter extends OncePerRequestFilter {

	public static final String HEADER = "Idempotency-Key";

	private final IdempotencyStore store;

	public IdempotencyFilter(IdempotencyStore store) {
		this.store = store;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		HttpMethod method = HttpMethod.valueOf(request.getMethod());
		return !(method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH
				|| method == HttpMethod.DELETE) || request.getHeader(HEADER) == null
				|| request.getHeader(HEADER).isBlank();
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String key = scope(request.getHeader(HEADER), request);
		var replay = store.find(key);
		if (replay.isPresent()) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setHeader(HEADER, "replayed");
			response.getOutputStream().write(replay.get().getBytes(StandardCharsets.UTF_8));
			return;
		}
		ContentCachingResponseWrapper wrapped = new ContentCachingResponseWrapper(response);
		chain.doFilter(request, wrapped);
		byte[] body = wrapped.getContentAsByteArray();
		if (wrapped.getStatus() < 400) {
			store.save(key, new String(body, StandardCharsets.UTF_8), wrapped.getStatus());
		}
		wrapped.copyBodyToResponse();
	}

	private String scope(String key, HttpServletRequest request) {
		String principal = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous";
		return principal + ":" + key.trim();
	}

}
