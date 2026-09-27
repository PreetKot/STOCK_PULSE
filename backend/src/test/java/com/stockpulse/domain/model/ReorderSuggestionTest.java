package com.stockpulse.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
class ReorderSuggestionTest {

    @Test
    void shouldCreateReorderSuggestionWithValidParameters() {
        // Given
        String id = "test-reorder-suggestion-1";
        String productId = "test-product-1";
        Integer currentStock = 5;
        Integer recommendedQuantity = 20;
        Integer leadTimeDays = 3;
        BigDecimal confidence = new BigDecimal("0.9200");
        String reasoning = "Low stock level requires immediate reorder";
        SuggestionStatus status = SuggestionStatus.PENDING;
        TriggerReason triggerReason = TriggerReason.INVENTORY_LOW;
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();

        // When
        ReorderSuggestion suggestion = new ReorderSuggestion(id, productId, currentStock,
                recommendedQuantity, leadTimeDays, confidence, reasoning, status, triggerReason, createdAt, updatedAt);

        // Then
        assertNotNull(suggestion);
        assertEquals(id, suggestion.getId());
        assertEquals(productId, suggestion.getProductId());
        assertEquals(currentStock, suggestion.getCurrentStock());
        assertEquals(recommendedQuantity, suggestion.getRecommendedQuantity());
        assertEquals(leadTimeDays, suggestion.getLeadTimeDays());
        assertEquals(confidence, suggestion.getConfidence());
        assertEquals(reasoning, suggestion.getReasoning());
        assertEquals(status, suggestion.getStatus());
        assertEquals(triggerReason, suggestion.getTriggerReason());
        assertEquals(createdAt, suggestion.getCreatedAt());
        assertEquals(updatedAt, suggestion.getUpdatedAt());
    }

    @Test
    void shouldValidatePositiveRecommendedQuantity() {
        // Given
        ReorderSuggestion suggestion = new ReorderSuggestion();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            suggestion.setRecommendedQuantity(-5);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            suggestion.setRecommendedQuantity(0);
        });
    }

    @Test
    void shouldAllowZeroOrPositiveCurrentStock() {
        // Given
        ReorderSuggestion suggestion = new ReorderSuggestion();

        // When
        suggestion.setCurrentStock(0);
        suggestion.setCurrentStock(10);

        // Then
        assertEquals(10, suggestion.getCurrentStock());
    }

    @Test
    void shouldValidateConfidenceBounds() {
        // Given
        ReorderSuggestion suggestion = new ReorderSuggestion();

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
        ReorderSuggestion suggestion = new ReorderSuggestion();
        BigDecimal validConfidence = new BigDecimal("0.8800");

        // When
        suggestion.setConfidence(validConfidence);

        // Then
        assertEquals(validConfidence, suggestion.getConfidence());
    }

    @Test
    void shouldAcceptPendingSuggestion() {
        // Given
        ReorderSuggestion suggestion = new ReorderSuggestion();
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
        ReorderSuggestion suggestion = new ReorderSuggestion();
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
        ReorderSuggestion suggestion = new ReorderSuggestion();
        suggestion.setStatus(SuggestionStatus.ACCEPTED);

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            suggestion.accept();
        });
    }

    @Test
    void shouldNotRejectAlreadyRejectedSuggestion() {
        // Given
        ReorderSuggestion suggestion = new ReorderSuggestion();
        suggestion.setStatus(SuggestionStatus.REJECTED);

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            suggestion.reject();
        });
    }

    @Test
    void shouldNotChangeStatusOfFinalizedSuggestion() {
        // Given
        ReorderSuggestion suggestion = new ReorderSuggestion();
        suggestion.setStatus(SuggestionStatus.ACCEPTED);

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            suggestion.setStatus(SuggestionStatus.PENDING);
        });
    }
}
