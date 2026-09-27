package com.stockpulse.service;

import com.stockpulse.domain.model.TriggerReason;

public record RecommendationRequested(String productId, TriggerReason triggerReason, boolean pricing, boolean reorder) {
    public RecommendationRequested(String productId, TriggerReason triggerReason) {
        this(productId, triggerReason, true, true);
    }
}
