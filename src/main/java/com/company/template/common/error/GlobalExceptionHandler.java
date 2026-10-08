package com.company.template.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Error-handling conventions (RFC 9457 Problem Details):
 * <ul>
 * <li>400 {@code TEMPLATE_VALIDATION_FAILED} for bean validation / malformed body.</li>
 * <li>400 {@code TEMPLATE_BAD_REQUEST} for {@code IllegalArgumentException}.</li>
 * <li>404 {@code TEMPLATE_NOT_FOUND} for {@code NoSuchElementException}.</li>
 * <li>500 {@code TEMPLATE_INTERNAL_ERROR} for anything else. Stack traces never
 * leak.</li>
 * </ul>
 * Every problem carries {@code correlationId} for log correlation.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
		String fields = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(e -> e.getField() + ": " + e.getDefaultMessage())
			.sorted()
			.collect(Collectors.joining("; "));
		ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Validation failed",
				fields.isEmpty() ? "Invalid request" : fields, req);
		problem.setProperty("code", "TEMPLATE_VALIDATION_FAILED");
		return problem;
	}

	@ExceptionHandler({ ConstraintViolationException.class, MethodArgumentTypeMismatchException.class,
			HttpMessageNotReadableException.class })
	public ProblemDetail handleBadRequest(Exception ex, HttpServletRequest req) {
		ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Bad request", rootMessage(ex), req);
		problem.setProperty("code", "TEMPLATE_BAD_REQUEST");
		return problem;
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ProblemDetail handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
		ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Bad request", ex.getMessage(), req);
		problem.setProperty("code", "TEMPLATE_BAD_REQUEST");
		return problem;
	}

	@ExceptionHandler(NoSuchElementException.class)
	public ProblemDetail handleNotFound(NoSuchElementException ex, HttpServletRequest req) {
		ProblemDetail problem = problem(HttpStatus.NOT_FOUND, "Not found", ex.getMessage(), req);
		problem.setProperty("code", "TEMPLATE_NOT_FOUND");
		return problem;
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest req) {
		log.error("Unhandled error on {} {}", req.getMethod(), req.getRequestURI(), ex);
		ProblemDetail problem = problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error",
				"Unexpected error. Contact support.", req);
		problem.setProperty("code", "TEMPLATE_INTERNAL_ERROR");
		return problem;
	}

	private ProblemDetail problem(HttpStatus status, String title, String detail, HttpServletRequest req) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setTitle(title);
		problem.setInstance(URI.create(req.getRequestURI()));
		String correlationId = MDC.get("correlationId");
		if (correlationId != null) {
			problem.setProperty("correlationId", correlationId);
		}
		return problem;
	}

	private String rootMessage(Exception ex) {
		Throwable t = ex;
		while (t.getCause() != null && t.getCause() != t) {
			t = t.getCause();
		}
		return t.getMessage() != null ? t.getMessage() : ex.getClass().getSimpleName();
	}

}
