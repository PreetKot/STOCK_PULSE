package com.stockpulse.web;

import com.stockpulse.api.ProductDtos.*;
import com.stockpulse.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody CreateProductRequest request) {
        return ProductResponse.from(service.create(request));
    }

    @GetMapping
    public List<ProductResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {
        return service.findByFilters(status, category).stream().map(ProductResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable String id) {
        return ProductResponse.from(service.findById(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable String id) {
        service.remove(id);
    }

    @PatchMapping("/{id}/stock")
    public ProductResponse updateStock(@PathVariable String id, @Valid @RequestBody UpdateStockRequest request) {
        return ProductResponse.from(service.updateStock(id, request.quantity()));
    }

    @PostMapping("/{id}/orders")
    public ProductResponse createOrder(@PathVariable String id, @Valid @RequestBody CreateOrderRequest request) {
        return ProductResponse.from(service.createOrder(id, request.quantity()));
    }

    @PostMapping("/{id}/suggest-pricing")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void requestPricingSuggestion(@PathVariable String id) {
        service.requestPricingSuggestion(id);
    }

    @PostMapping("/{id}/suggest-reorder")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void requestReorderSuggestion(@PathVariable String id) {
        service.requestReorderSuggestion(id);
    }
}