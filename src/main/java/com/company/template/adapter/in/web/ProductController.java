package com.company.template.adapter.in.web;

import com.company.template.adapter.in.web.ProductDtos.CreateProductRequest;
import com.company.template.adapter.in.web.ProductDtos.ProductResponse;
import com.company.template.app.port.in.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sample REST adapter. Thin by convention: HTTP + validation + mapping only, business
 * logic lives in {@link ProductService}.
 */
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "products", description = "Sample product operations (replace per service)")
public class ProductController {

	private final ProductService service;

	public ProductController(ProductService service) {
		this.service = service;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a product")
	public ProductResponse create(@Valid @RequestBody CreateProductRequest request) {
		return ProductMapper.toResponse(service.create(request.name(), request.description(), request.priceCents()));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a product by id")
	public ProductResponse get(@PathVariable String id) {
		return ProductMapper.toResponse(service.get(id));
	}

	@GetMapping
	@Operation(summary = "List products")
	public List<ProductResponse> list(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return service.list(page, size).stream().map(ProductMapper::toResponse).toList();
	}

}
