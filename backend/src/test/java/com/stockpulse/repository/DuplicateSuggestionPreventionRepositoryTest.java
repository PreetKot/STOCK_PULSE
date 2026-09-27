package com.stockpulse.repository;

import com.stockpulse.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@SuppressWarnings({"ResultOfMethodCallIgnored", "unused"})
class DuplicateSuggestionPreventionRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PricingSuggestionRepository pricingSuggestionRepository;

    @Autowired
    private ReorderSuggestionRepository reorderSuggestionRepository;

    @Test
    void shouldPreventDuplicatePendingPricingSuggestionsAtDatabaseLevel() {
        // Given a product
        String productId = UUID.randomUUID().toString();
        Product product = new Product(
            productId,
            "TEST-SKU-001",
            "Test Product",
            ProductCategory.ELECTRONICS,
            new BigDecimal("99.99"),
            10,
            5,
            3,
            ProductStatus.ACTIVE,
            Instant.now(),
            Instant.now()
        );
        productRepository.save(product);

        // And a pending pricing suggestion
        String suggestionId1 = UUID.randomUUID().toString();
        PricingSuggestion suggestion1 = new PricingSuggestion(
            suggestionId1,
            productId,
            new BigDecimal("99.99"),
            new BigDecimal("109.99"),
            PricingDirection.INCREASE,
            new BigDecimal("0.8500"),
            "Test reasoning",
            SuggestionStatus.PENDING,
            TriggerReason.DEMAND_SPIKE,
            Instant.now(),
            Instant.now()
        );
        pricingSuggestionRepository.save(suggestion1);

        // When trying to save another pending suggestion with same product and trigger reason
        String suggestionId2 = UUID.randomUUID().toString();
        PricingSuggestion suggestion2 = new PricingSuggestion(
            suggestionId2,
            productId,
            new BigDecimal("99.99"),
            new BigDecimal("119.99"),
            PricingDirection.INCREASE,
            new BigDecimal("0.9000"),
            "Different reasoning",
            SuggestionStatus.PENDING,
            TriggerReason.DEMAND_SPIKE,
            Instant.now(),
            Instant.now()
        );

        // Then it should fail due to database constraint
        assertThrows(DataIntegrityViolationException.class, () -> {
            pricingSuggestionRepository.saveAndFlush(suggestion2);
        });
    }

    @Test
    void shouldAllowMultipleNonPendingPricingSuggestions() {
        // Given a product
        String productId = UUID.randomUUID().toString();
        Product product = new Product(
            productId,
            "TEST-SKU-002",
            "Test Product 2",
            ProductCategory.APPAREL,
            new BigDecimal("49.99"),
            15,
            8,
            2,
            ProductStatus.ACTIVE,
            Instant.now(),
            Instant.now()
        );
        productRepository.save(product);

        // And an accepted pricing suggestion
        String acceptedSuggestionId = UUID.randomUUID().toString();
        PricingSuggestion acceptedSuggestion = new PricingSuggestion(
            acceptedSuggestionId,
            productId,
            new BigDecimal("49.99"),
            new BigDecimal("59.99"),
            PricingDirection.INCREASE,
            new BigDecimal("0.8500"),
            "Test reasoning",
            SuggestionStatus.ACCEPTED,
            TriggerReason.DEMAND_SPIKE,
            Instant.now(),
            Instant.now()
        );
        pricingSuggestionRepository.save(acceptedSuggestion);

        // And a rejected pricing suggestion with same product and trigger reason
        String rejectedSuggestionId = UUID.randomUUID().toString();
        PricingSuggestion rejectedSuggestion = new PricingSuggestion(
            rejectedSuggestionId,
            productId,
            new BigDecimal("49.99"),
            new BigDecimal("54.99"),
            PricingDirection.INCREASE,
            new BigDecimal("0.7500"),
            "Different reasoning",
            SuggestionStatus.REJECTED,
            TriggerReason.DEMAND_SPIKE,
            Instant.now(),
            Instant.now()
        );

        // Then both should be saved successfully
        assertDoesNotThrow(() -> {
            pricingSuggestionRepository.save(rejectedSuggestion);
        });

        assertEquals(2, pricingSuggestionRepository.findByProductId(productId).size());
    }
}
