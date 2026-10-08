# Workflow: add a REST endpoint

1. DTOs in `adapter/in/web/*Dtos.java`: records + Jakarta validation annotations.
2. Extend the inbound port (`app/port/in/`) with the use case; implement in
   `app/service/` (constructor injection, `Clock`, no Spring-Data types).
3. Map with a manual mapper (`ProductMapper` style). No MapStruct.
4. Thin controller: HTTP + `@Valid` + mapper only. Errors map automatically via
   `GlobalExceptionHandler`; add a new `TEMPLATE_*` code only for a new category.
5. Tests: use-case unit test (Mockito, fixed `Clock`) + `@WebMvcTest` slice test
   (mock the port with Framework `@MockitoBean`; disable idempotency via
   `app.modules.idempotency.enabled=false` unless testing it).
6. Run `mvn spring-javaformat:apply`, then `mvn verify`.
