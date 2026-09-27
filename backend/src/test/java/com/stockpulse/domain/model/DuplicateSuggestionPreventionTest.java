package com.stockpulse.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class DuplicateSuggestionPreventionTest {
    @Test
    void pendingSuggestionsMayBeCreatedIndividuallyForTheSameTrigger() {
        var first = new PricingSuggestion("p", BigDecimal.TEN, BigDecimal.valueOf(11), PricingDirection.INCREASE, BigDecimal.valueOf(.8), "reason", SuggestionStatus.PENDING, TriggerReason.DEMAND_SPIKE);
        var second = new PricingSuggestion("p", BigDecimal.TEN, BigDecimal.valueOf(11), PricingDirection.INCREASE, BigDecimal.valueOf(.8), "reason", SuggestionStatus.PENDING, TriggerReason.DEMAND_SPIKE);
        assertNotEquals(first.getId(), second.getId());
        assertEquals(SuggestionStatus.PENDING, first.getStatus());
        assertEquals(SuggestionStatus.PENDING, second.getStatus());
    }

    @Test
    void finalizedSuggestionsCanShareAProductAndTrigger() {
        var accepted = new PricingSuggestion("p", BigDecimal.TEN, BigDecimal.valueOf(11), PricingDirection.INCREASE, BigDecimal.valueOf(.8), "reason", SuggestionStatus.ACCEPTED, TriggerReason.DEMAND_SPIKE);
        var rejected = new PricingSuggestion("p", BigDecimal.TEN, BigDecimal.valueOf(11), PricingDirection.INCREASE, BigDecimal.valueOf(.8), "reason", SuggestionStatus.REJECTED, TriggerReason.DEMAND_SPIKE);
        assertTrue(accepted.isAccepted());
        assertTrue(rejected.isRejected());
    }

    @Test
    void reorderSuggestionValuesRemainValid() {
        var suggestion = new ReorderSuggestion("p", 5, 20, 3, BigDecimal.valueOf(.9), "reason", SuggestionStatus.PENDING, TriggerReason.INVENTORY_LOW);
        assertTrue(suggestion.isPending());
        assertThrows(IllegalArgumentException.class, () -> suggestion.setRecommendedQuantity(0));
    }
}
