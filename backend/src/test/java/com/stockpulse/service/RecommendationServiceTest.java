package com.stockpulse.service;

import com.stockpulse.commerce.RuleBasedCommerceStrategy;
import com.stockpulse.domain.model.*;
import com.stockpulse.repository.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Optional;
import static org.mockito.Mockito.*;

class RecommendationServiceTest {
    private final ProductRepository products = mock(ProductRepository.class);
    private final PricingSuggestionRepository pricing = mock(PricingSuggestionRepository.class);
    private final ReorderSuggestionRepository reorder = mock(ReorderSuggestionRepository.class);
    private final RecommendationService service = new RecommendationService(products, pricing, reorder, new RuleBasedCommerceStrategy());

    @Test void duplicatePendingSuggestionsAreNotSaved() {
        var product = new Product("SKU", "Test product", ProductCategory.ELECTRONICS, BigDecimal.TEN, 2, 10, 4);
        when(products.findById(product.getId())).thenReturn(Optional.of(product));
        when(products.averageDemandVelocity(ProductCategory.ELECTRONICS)).thenReturn(5d);
        when(pricing.existsPendingSuggestion(product.getId(), TriggerReason.INVENTORY_LOW)).thenReturn(true);
        when(reorder.existsPendingSuggestion(product.getId(), TriggerReason.INVENTORY_LOW)).thenReturn(true);

        service.generate(product.getId(), TriggerReason.INVENTORY_LOW);

        verify(pricing, never()).save(any());
        verify(reorder, never()).save(any());
    }

    @Test void ruleRecommendationsArePersistedAsPending() {
        var product = new Product("SKU", "Test product", ProductCategory.ELECTRONICS, BigDecimal.TEN, 2, 10, 4);
        when(products.findById(product.getId())).thenReturn(Optional.of(product));
        when(products.averageDemandVelocity(ProductCategory.ELECTRONICS)).thenReturn(5d);
        when(pricing.existsPendingSuggestion(product.getId(), TriggerReason.INVENTORY_LOW)).thenReturn(false);
        when(reorder.existsPendingSuggestion(product.getId(), TriggerReason.INVENTORY_LOW)).thenReturn(false);

        service.generate(product.getId(), TriggerReason.INVENTORY_LOW);

        verify(pricing).save(argThat(s -> s.getStatus() == SuggestionStatus.PENDING && s.getProductId().equals(product.getId())));
        verify(reorder).save(argThat(s -> s.getStatus() == SuggestionStatus.PENDING && s.getProductId().equals(product.getId())));
    }
}
