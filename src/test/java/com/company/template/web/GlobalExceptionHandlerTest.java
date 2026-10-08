package com.company.template.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.template.common.config.CommonConfig;
import com.company.template.common.config.OpenApiConfig;
import com.company.template.common.error.GlobalExceptionHandler;
import jakarta.validation.ConstraintViolationException;
import java.util.NoSuchElementException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;

/** Direct handler tests for branches the web slice does not hit (500s, misc 400s). */
class GlobalExceptionHandlerTest {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/products/x");

	@Test
	void unexpectedErrorMapsTo500WithoutLeakingDetail() {
		ProblemDetail problem = handler.handleUnexpected(new RuntimeException("db password=hunter2"), request);

		assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
		assertThat(problem.getProperties()).containsEntry("code", "TEMPLATE_INTERNAL_ERROR");
		assertThat(problem.getDetail()).doesNotContain("hunter2");
	}

	@Test
	void problemCarriesCorrelationIdFromMdc() {
		MDC.put("correlationId", "corr-1");
		try {
			ProblemDetail problem = handler.handleNotFound(new NoSuchElementException("gone"), request);
			assertThat(problem.getProperties()).containsEntry("code", "TEMPLATE_NOT_FOUND")
				.containsEntry("correlationId", "corr-1");
		}
		finally {
			MDC.remove("correlationId");
		}
	}

	@Test
	void constraintViolationsMapTo400() {
		ProblemDetail problem = handler.handleBadRequest(new ConstraintViolationException("bad", Set.of()), request);

		assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
		assertThat(problem.getProperties()).containsEntry("code", "TEMPLATE_BAD_REQUEST");
	}

	@Test
	void illegalArgumentMapsTo400() {
		ProblemDetail problem = handler.handleIllegalArgument(new IllegalArgumentException("nope"), request);

		assertThat(problem.getDetail()).isEqualTo("nope");
	}

	@Test
	void nestedCauseUnwrapsToRootMessage() {
		RuntimeException root = new RuntimeException("root cause");
		ProblemDetail problem = handler.handleBadRequest(new IllegalStateException("wrapper", root), request);

		assertThat(problem.getDetail()).isEqualTo("root cause");
	}

	@Test
	void sharedConfigBeansProduceSaneDefaults() {
		assertThat(new CommonConfig().clock()).isNotNull();
		assertThat(new OpenApiConfig().api("svc", "v1").getInfo().getTitle()).isEqualTo("svc API");
	}

}
