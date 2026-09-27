package com.stockpulse.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnumValidationTest {

    @Test
    void shouldHaveValidProductCategories() {
        ProductCategory[] categories = ProductCategory.values();
        assertEquals(4, categories.length);
        assertEquals(ProductCategory.ELECTRONICS, categories[0]);
        assertEquals(ProductCategory.APPAREL, categories[1]);
        assertEquals(ProductCategory.HOME, categories[2]);
        assertEquals(ProductCategory.UNSPECIFIED, categories[3]);
    }

    @Test
    void shouldHaveValidProductStatuses() {
        ProductStatus[] statuses = ProductStatus.values();
        assertEquals(4, statuses.length);
        assertEquals(ProductStatus.ACTIVE, statuses[0]);
        assertEquals(ProductStatus.PRICE_REVIEW_PENDING, statuses[1]);
        assertEquals(ProductStatus.OUT_OF_STOCK, statuses[2]);
        assertEquals(ProductStatus.INACTIVE, statuses[3]);
    }

    @Test
    void shouldHaveValidSuggestionStatuses() {
        SuggestionStatus[] statuses = SuggestionStatus.values();
        assertEquals(3, statuses.length);
        assertEquals(SuggestionStatus.PENDING, statuses[0]);
        assertEquals(SuggestionStatus.ACCEPTED, statuses[1]);
        assertEquals(SuggestionStatus.REJECTED, statuses[2]);
    }

    @Test
    void shouldHaveValidPricingDirections() {
        PricingDirection[] directions = PricingDirection.values();
        assertEquals(3, directions.length);
        assertEquals(PricingDirection.INCREASE, directions[0]);
        assertEquals(PricingDirection.DECREASE, directions[1]);
        assertEquals(PricingDirection.HOLD, directions[2]);
    }

    @Test
    void shouldHaveValidTriggerReasons() {
        TriggerReason[] reasons = TriggerReason.values();
        assertEquals(4, reasons.length);
        assertEquals(TriggerReason.INITIAL, reasons[0]);
        assertEquals(TriggerReason.INVENTORY_LOW, reasons[1]);
        assertEquals(TriggerReason.DEMAND_SPIKE, reasons[2]);
        assertEquals(TriggerReason.MANUAL, reasons[3]);
    }
}
