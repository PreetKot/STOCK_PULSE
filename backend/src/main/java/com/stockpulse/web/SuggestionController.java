package com.stockpulse.web;

import com.stockpulse.api.ProductDtos.*;
import com.stockpulse.service.SuggestionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SuggestionController {

    private final SuggestionService service;

    public SuggestionController(SuggestionService service) {
        this.service = service;
    }

    @GetMapping("/pricing-suggestions")
    public List<PricingSuggestionResponse> listPendingPricingSuggestions() {
        return service.listPendingPricingSuggestions();
    }

    @GetMapping("/reorder-suggestions")
    public List<ReorderSuggestionResponse> listPendingReorderSuggestions() {
        return service.listPendingReorderSuggestions();
    }

    @PatchMapping("/pricing-suggestions/{id}")
    public PricingSuggestionResponse decidePricingSuggestion(@PathVariable String id, @Valid @RequestBody DecisionRequest request) {
        return service.decidePricingSuggestion(id, request);
    }

    @PatchMapping("/reorder-suggestions/{id}")
    public ReorderSuggestionResponse decideReorderSuggestion(@PathVariable String id, @Valid @RequestBody DecisionRequest request) {
        return service.decideReorderSuggestion(id, request);
    }
}