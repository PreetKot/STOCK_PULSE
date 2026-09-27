package com.stockpulse.commerce;

import com.stockpulse.domain.model.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class RuleBasedCommerceStrategyTest {
    private final RuleBasedCommerceStrategy strategy = new RuleBasedCommerceStrategy();

    @Test void lowInventoryUsesTenPercentIncrease() {
        var product = product(24.99, 8, 15, 12);
        var result = strategy.recommendPricing(new CommerceContext(product, 5, TriggerReason.INVENTORY_LOW));
        assertEquals(new BigDecimal("27.49"), result.recommendedPrice());
        assertEquals(PricingDirection.INCREASE, result.direction());
    }

    @Test void demandSpikeUsesFivePercentIncreaseWhenInventoryIsHealthy() {
        var product = product(100, 30, 10, 15);
        var result = strategy.recommendPricing(new CommerceContext(product, 5, TriggerReason.DEMAND_SPIKE));
        assertEquals(new BigDecimal("105.00"), result.recommendedPrice());
        assertEquals(PricingDirection.INCREASE, result.direction());
    }

    @Test void normalConditionsHoldPrice() {
        var product = product(100, 30, 10, 10);
        var result = strategy.recommendPricing(new CommerceContext(product, 5, TriggerReason.MANUAL));
        assertEquals(0, new BigDecimal("100.00").compareTo(result.recommendedPrice()));
        assertEquals(PricingDirection.HOLD, result.direction());
    }

    @Test void lowInventoryTakesPrecedenceOverDemandSpike() {
        var result = strategy.recommendPricing(new CommerceContext(product(100, 2, 10, 50), 5, TriggerReason.DEMAND_SPIKE));
        assertEquals(new BigDecimal("110.00"), result.recommendedPrice());
    }

    @Test void reorderQuantityUsesCoverageDeficit() {
        var result = strategy.recommendReorder(new CommerceContext(product(10, 8, 10, 1), 5, TriggerReason.INVENTORY_LOW));
        assertEquals(22, result.recommendedQuantity());
    }

    @Test void reorderQuantityHasMinimumOfOne() {
        var result = strategy.recommendReorder(new CommerceContext(product(10, 20, 2, 1), 5, TriggerReason.MANUAL));
        assertEquals(1, result.recommendedQuantity());
    }

    private Product product(double price, int stock, int threshold, int velocity) {
        return new Product("SKU", "Test product", ProductCategory.ELECTRONICS, BigDecimal.valueOf(price), stock, threshold, velocity);
    }
}
