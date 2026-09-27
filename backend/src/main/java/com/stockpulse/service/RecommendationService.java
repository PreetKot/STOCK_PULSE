package com.stockpulse.service;

import com.stockpulse.commerce.*;
import com.stockpulse.domain.model.*;
import com.stockpulse.repository.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationService {
    private final ProductRepository products;
    private final PricingSuggestionRepository pricing;
    private final ReorderSuggestionRepository reorder;
    private final CommerceStrategy strategy;
    public RecommendationService(ProductRepository products, PricingSuggestionRepository pricing, ReorderSuggestionRepository reorder, CommerceStrategy strategy) { this.products = products; this.pricing = pricing; this.reorder = reorder; this.strategy = strategy; }

    public void generate(String productId, TriggerReason reason) {
        generate(productId, reason, true, true);
    }

    @Async
    @Transactional
    public void generate(String productId, TriggerReason reason, boolean includePricing, boolean includeReorder) {
        Product product = products.findById(productId).orElseThrow();
        CommerceContext context = new CommerceContext(product, products.averageDemandVelocity(product.getCategory()), reason);
        if (includePricing && !pricing.existsPendingSuggestion(productId, reason)) pricing.save(toPricing(product, strategy.recommendPricing(context), reason));
        if (includeReorder && !reorder.existsPendingSuggestion(productId, reason)) reorder.save(toReorder(product, strategy.recommendReorder(context), reason));
    }

    private PricingSuggestion toPricing(Product p, PricingRecommendation r, TriggerReason reason) { return new PricingSuggestion(p.getId(), p.getCurrentPrice(), r.recommendedPrice(), r.direction(), r.confidence(), r.reasoning(), SuggestionStatus.PENDING, reason); }
    private ReorderSuggestion toReorder(Product p, ReorderRecommendation r, TriggerReason reason) { return new ReorderSuggestion(p.getId(), p.getStockLevel(), r.recommendedQuantity(), r.leadTimeDays(), r.confidence(), r.reasoning(), SuggestionStatus.PENDING, reason); }
}
