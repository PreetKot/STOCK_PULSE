package com.stockpulse.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
class PricingSuggestionTest {

    @Test
    void shouldCreatePricingSuggestionWithValidParameters() {
        // Given
        String id = "test-pricing-suggestion-1";
        String productId = "test-product-1";
        BigDecimal currentPrice = new BigDecimal("99.99");
        BigDecimal recommendedPrice = new BigDecimal("109.99");
        PricingDirection direction = PricingDirection.INCREASE;
        BigDecimal confidence = new BigDecimal("0.8500");
        String reasoning = "Price increase justified by market demand";
        SuggestionStatus status = SuggestionStatus.PENDING;
        TriggerReason triggerReason = TriggerReason.DEMAND_SPIKE;
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();

        // When
        PricingSuggestion suggestion = new PricingSuggestion(id, productId, currentPrice,
                recommendedPrice, direction, confidence, reasoning, status, triggerReason, createdAt, updatedAt);

        // Then
        assertNotNull(suggestion);
        assertEquals(id, suggestion.getId());
        assertEquals(productId, suggestion.getProductId());
        assertEquals(currentPrice, suggestion.getCurrentPrice());
        assertEquals(recommendedPrice, suggestion.getRecommendedPrice());
        assertEquals(direction, suggestion.getDirection());
        assertEquals(confidence, suggestion.getConfidence());
        assertEquals(reasoning, suggestion.getReasoning());
        assertEquals(status, suggestion.getStatus());
        assertEquals(triggerReason, suggestion.getTriggerReason());
        assertEquals(createdAt, suggestion.getCreatedAt());
        assertEquals(updatedAt, suggestion.getUpdatedAt());
    }

    @Test
    void shouldValidatePositivePrices() {
        // Given
        PricingSuggestion suggestion = new PricingSuggestion();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            suggestion.setCurrentPrice(new BigDecimal("-10.00"));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            suggestion.setRecommendedPrice(new BigDecimal("-5.00"));
        });
    }

    @Test
    void shouldValidateConfidenceBounds() {
        // Given
        PricingSuggestion suggestion = new PricingSuggestion();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            suggestion.setConfidence(new BigDecimal("-0.1000"));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            suggestion.setConfidence(new BigDecimal("1.5000"));
        });
    }

    @Test
    void shouldAllowValidConfidenceValues() {
        // Given
        PricingSuggestion suggestion = new PricingSuggestion();
        BigDecimal validConfidence = new BigDecimal("0.7500");

        // When
        suggestion.setConfidence(validConfidence);

        // Then
        assertEquals(validConfidence, suggestion.getConfidence());
    }

    @Test
    void shouldAcceptPendingSuggestion() {
        // Given
        PricingSuggestion suggestion = new PricingSuggestion();
        suggestion.setStatus(SuggestionStatus.PENDING);

        // When
        suggestion.accept();

        // Then
        assertEquals(SuggestionStatus.ACCEPTED, suggestion.getStatus());
        assertTrue(suggestion.isAccepted());
        assertFalse(suggestion.isPending());
    }

    @Test
    void shouldRejectPendingSuggestion() {
        // Given
        PricingSuggestion suggestion = new PricingSuggestion();
        suggestion.setStatus(SuggestionStatus.PENDING);

        // When
        suggestion.reject();

        // Then
        assertEquals(SuggestionStatus.REJECTED, suggestion.getStatus());
        assertTrue(suggestion.isRejected());
        assertFalse(suggestion.isPending());
    }

    @Test
    void shouldNotAcceptAlreadyAcceptedSuggestion() {
        // Given
        PricingSuggestion suggestion = new PricingSuggestion();
        suggestion.setStatus(SuggestionStatus.ACCEPTED);

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            suggestion.accept();
        });
    }

    @Test
    void shouldNotRejectAlreadyRejectedSuggestion() {
        // Given
        PricingSuggestion suggestion = new PricingSuggestion();
        suggestion.setStatus(SuggestionStatus.REJECTED);

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            suggestion.reject();
        });
    }

    @Test
    void shouldNotChangeStatusOfFinalizedSuggestion() {
        // Given
        PricingSuggestion suggestion = new PricingSuggestion();
        suggestion.setStatus(SuggestionStatus.ACCEPTED);

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            suggestion.setStatus(SuggestionStatus.PENDING);
        });
    }
}
