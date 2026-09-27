package com.stockpulse.web;

import com.stockpulse.ai.AiCommerceStrategy;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/strategy")
public class StrategyController {
    private final AiCommerceStrategy strategy;

    public StrategyController(AiCommerceStrategy strategy) {
        this.strategy = strategy;
    }

    @GetMapping
    public StrategyResponse current() {
        return new StrategyResponse(strategy.mode(), strategy.aiConfigured());
    }

    @PutMapping
    public StrategyResponse update(@Valid @RequestBody StrategyRequest request) {
        strategy.setMode(request.mode());
        return current();
    }

    public record StrategyRequest(@NotBlank String mode) { }
    public record StrategyResponse(String mode, boolean aiConfigured) { }
}
