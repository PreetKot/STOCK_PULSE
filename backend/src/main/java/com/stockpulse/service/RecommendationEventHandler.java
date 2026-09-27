package com.stockpulse.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class RecommendationEventHandler {
    private final RecommendationService recommendations;
    public RecommendationEventHandler(RecommendationService recommendations) { this.recommendations = recommendations; }
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRecommendationRequested(RecommendationRequested event) { recommendations.generate(event.productId(), event.triggerReason(), event.pricing(), event.reorder()); }
}
