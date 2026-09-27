package com.stockpulse.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.stockpulse.commerce.*;
import com.stockpulse.domain.model.PricingDirection;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Primary
@Component
public class AiCommerceStrategy implements CommerceStrategy {
    private final LLMGateway gateway;
    private final RuleBasedCommerceStrategy fallback;
    private volatile String strategyMode;
    public AiCommerceStrategy(LLMGateway gateway, RuleBasedCommerceStrategy fallback, @Value("${commerce.strategy:rule}") String strategyMode) { this.gateway = gateway; this.fallback = fallback; setMode(strategyMode); }

    public String mode() { return strategyMode; }
    public boolean aiConfigured() { return gateway.configured(); }
    public void setMode(String mode) {
        if (mode == null || !(mode.equalsIgnoreCase("rule") || mode.equalsIgnoreCase("ai"))) {
            throw new IllegalArgumentException("Strategy mode must be RULE or AI");
        }
        this.strategyMode = mode.toUpperCase(java.util.Locale.ROOT);
    }

    @Override public PricingRecommendation recommendPricing(CommerceContext context) {
        if (!"ai".equalsIgnoreCase(strategyMode)) return fallback.recommendPricing(context);
        try {
            JsonNode json = gateway.call(pricingPrompt(context));
            BigDecimal price = decimal(json, "recommendedPrice"); BigDecimal confidence = confidence(json);
            PricingDirection direction = PricingDirection.valueOf(json.path("direction").asText());
            if (price.signum() <= 0 || price.compareTo(context.product().getCurrentPrice().multiply(BigDecimal.TEN)) > 0 || direction == null) throw new IllegalArgumentException("Invalid AI price response");
            return new PricingRecommendation(price.setScale(2, java.math.RoundingMode.HALF_UP), direction, confidence, text(json, "reasoning"));
        } catch (RuntimeException ignored) { return fallback.recommendPricing(context); }
    }

    @Override public ReorderRecommendation recommendReorder(CommerceContext context) {
        if (!"ai".equalsIgnoreCase(strategyMode)) return fallback.recommendReorder(context);
        try {
            JsonNode json = gateway.call(reorderPrompt(context));
            int quantity = json.path("recommendedQuantity").asInt(-1); BigDecimal confidence = confidence(json);
            if (quantity < 1) throw new IllegalArgumentException("Invalid AI reorder response");
            return new ReorderRecommendation(quantity, Math.max(0, json.path("leadTimeDays").asInt(3)), confidence, text(json, "reasoning"));
        } catch (RuntimeException ignored) { return fallback.recommendReorder(context); }
    }

    private String pricingPrompt(CommerceContext c) {
        var p = c.product(); String trigger = c.triggerReason().name();
        String focus = "INVENTORY_LOW".equals(trigger)
                ? "Discuss protecting scarce inventory versus clearing stock and explain that tradeoff."
                : "Discuss demand momentum versus the category average and recommend a controlled, modest pricing response.";
        return "You are a pricing commerce advisor. Return a JSON object only. Fields: recommendedPrice (positive JSON number), direction (exactly INCREASE, DECREASE, or HOLD), confidence (JSON number between 0 and 1, never text such as HIGH), reasoning (brief plain-English string). Product context: name=" + p.getName() + ", category=" + p.getCategory() + ", currentPrice=" + p.getCurrentPrice() + ", stockLevel=" + p.getStockLevel() + ", reorderThreshold=" + p.getReorderThreshold() + ", demandVelocity=" + p.getDemandVelocity() + ", categoryAverageVelocity=" + c.categoryAverageVelocity() + ", triggerReason=" + trigger + ". " + focus;
    }

    private String reorderPrompt(CommerceContext c) {
        var p = c.product(); String trigger = c.triggerReason().name();
        String focus = "DEMAND_SPIKE".equals(trigger)
                ? "Focus on preserving availability during elevated demand and explain replenishment coverage."
                : "Focus on restoring coverage from current stock toward the reorder threshold and explain lead time.";
        return "You are a replenishment commerce advisor. Return a JSON object only. Fields: recommendedQuantity (positive JSON integer), leadTimeDays (nonnegative JSON integer), confidence (JSON number between 0 and 1, never text such as HIGH), reasoning (brief plain-English string). Product context: name=" + p.getName() + ", category=" + p.getCategory() + ", currentPrice=" + p.getCurrentPrice() + ", stockLevel=" + p.getStockLevel() + ", reorderThreshold=" + p.getReorderThreshold() + ", demandVelocity=" + p.getDemandVelocity() + ", categoryAverageVelocity=" + c.categoryAverageVelocity() + ", triggerReason=" + trigger + ". " + focus;
    }
    private BigDecimal decimal(JsonNode node, String field) { if (!node.has(field) || !node.get(field).isNumber()) throw new IllegalArgumentException("Missing AI field"); return node.get(field).decimalValue(); }
    private BigDecimal confidence(JsonNode node) { BigDecimal value = decimal(node, "confidence"); if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.ONE) > 0) throw new IllegalArgumentException("Invalid confidence"); return value; }
    private String text(JsonNode node, String field) { String value = node.path(field).asText(); if (value.isBlank()) throw new IllegalArgumentException("Missing AI reasoning"); return value; }
}
