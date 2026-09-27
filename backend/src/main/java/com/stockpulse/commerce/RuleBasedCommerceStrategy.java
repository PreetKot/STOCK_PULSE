package com.stockpulse.commerce;

import com.stockpulse.domain.model.PricingDirection;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class RuleBasedCommerceStrategy implements CommerceStrategy {
    @Override
    public PricingRecommendation recommendPricing(CommerceContext context) {
        var product = context.product();
        if (product.getStockLevel() < product.getReorderThreshold()) {
            return new PricingRecommendation(product.getCurrentPrice().multiply(BigDecimal.valueOf(1.10)).setScale(2, RoundingMode.HALF_UP),
                    PricingDirection.INCREASE, BigDecimal.valueOf(0.90), "Stock is below the reorder threshold, so a controlled 10% increase protects scarce inventory.");
        }
        if (context.categoryAverageVelocity() > 0 && product.getDemandVelocity() > context.categoryAverageVelocity() * 2) {
            return new PricingRecommendation(product.getCurrentPrice().multiply(BigDecimal.valueOf(1.05)).setScale(2, RoundingMode.HALF_UP),
                    PricingDirection.INCREASE, BigDecimal.valueOf(0.82), "Demand is more than twice the category average, so a modest 5% increase responds to momentum.");
        }
        return new PricingRecommendation(product.getCurrentPrice(), PricingDirection.HOLD, BigDecimal.valueOf(0.70), "Inventory and demand are within the normal range; holding price avoids unnecessary movement.");
    }

    @Override
    public ReorderRecommendation recommendReorder(CommerceContext context) {
        var product = context.product();
        int quantity = Math.max(1, product.getReorderThreshold() * 3 - product.getStockLevel());
        return new ReorderRecommendation(quantity, 3, BigDecimal.valueOf(0.88),
                "Replenishment restores approximately three reorder-thresholds of coverage based on current stock.");
    }
}