package com.stockpulse.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockpulse.commerce.*;
import com.stockpulse.domain.model.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AiCommerceStrategyTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final LLMGateway gateway = mock(LLMGateway.class);
    private final AiCommerceStrategy strategy = new AiCommerceStrategy(gateway, new RuleBasedCommerceStrategy(), "ai");
    private final CommerceContext lowStock = new CommerceContext(product(8, 15, 12), 5, TriggerReason.INVENTORY_LOW);

    @Test void validPricingResponseIsAccepted() throws Exception {
        when(gateway.call(anyString())).thenReturn(mapper.readTree("{\"recommendedPrice\":27.49,\"direction\":\"INCREASE\",\"confidence\":0.82,\"reasoning\":\"Protects scarce inventory.\"}"));
        var result = strategy.recommendPricing(lowStock);
        assertEquals(new BigDecimal("27.49"), result.recommendedPrice());
        assertEquals(PricingDirection.INCREASE, result.direction());
    }

    @Test void validReorderResponseIsAccepted() throws Exception {
        when(gateway.call(anyString())).thenReturn(mapper.readTree("{\"recommendedQuantity\":22,\"leadTimeDays\":3,\"confidence\":0.78,\"reasoning\":\"Restores coverage.\"}"));
        var result = strategy.recommendReorder(lowStock);
        assertEquals(22, result.recommendedQuantity());
    }

    @Test void triggerPromptsAreMateriallyDifferent() throws Exception {
        when(gateway.call(anyString())).thenReturn(mapper.readTree("{\"recommendedPrice\":27.49,\"direction\":\"INCREASE\",\"confidence\":0.82,\"reasoning\":\"Reason.\"}"));
        strategy.recommendPricing(lowStock);
        strategy.recommendPricing(new CommerceContext(product(30, 10, 15), 5, TriggerReason.DEMAND_SPIKE));
        var prompts = mockingDetails(gateway).getInvocations().stream().map(invocation -> (String) invocation.getArguments()[0]).toList();
        assertTrue(prompts.get(0).contains("scarce inventory"));
        assertTrue(prompts.get(1).contains("demand momentum"));
    }

    @Test void invalidJsonNodeFallsBack() throws Exception {
        when(gateway.call(anyString())).thenReturn(mapper.readTree("{\"confidence\":0.8}"));
        assertEquals(new BigDecimal("27.49"), strategy.recommendPricing(lowStock).recommendedPrice());
    }

    @Test void negativeAndUnrealisticPricesFallBack() throws Exception {
        when(gateway.call(anyString())).thenReturn(mapper.readTree("{\"recommendedPrice\":-1,\"direction\":\"INCREASE\",\"confidence\":0.8,\"reasoning\":\"bad\"}"));
        assertEquals(new BigDecimal("27.49"), strategy.recommendPricing(lowStock).recommendedPrice());
        when(gateway.call(anyString())).thenReturn(mapper.readTree("{\"recommendedPrice\":9999,\"direction\":\"INCREASE\",\"confidence\":0.8,\"reasoning\":\"bad\"}"));
        assertEquals(new BigDecimal("27.49"), strategy.recommendPricing(lowStock).recommendedPrice());
    }

    @Test void invalidReorderQuantityFallsBack() throws Exception {
        when(gateway.call(anyString())).thenReturn(mapper.readTree("{\"recommendedQuantity\":0,\"confidence\":0.8,\"reasoning\":\"bad\"}"));
        assertEquals(37, strategy.recommendReorder(lowStock).recommendedQuantity());
    }

    @Test void networkFailureFallsBack() {
        when(gateway.call(anyString())).thenThrow(new IllegalStateException("network failure"));
        assertEquals(new BigDecimal("27.49"), strategy.recommendPricing(lowStock).recommendedPrice());
        assertEquals(37, strategy.recommendReorder(lowStock).recommendedQuantity());
    }

    @Test void ruleModeDoesNotCallGateway() {
        var ruleStrategy = new AiCommerceStrategy(gateway, new RuleBasedCommerceStrategy(), "rule");
        ruleStrategy.recommendPricing(lowStock);
        verifyNoInteractions(gateway);
    }

    private Product product(int stock, int threshold, int velocity) { return new Product("AI-SKU", "AI test product", ProductCategory.APPAREL, BigDecimal.valueOf(24.99), stock, threshold, velocity); }
}
