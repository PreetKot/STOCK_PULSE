package com.stockpulse.service;

import com.stockpulse.api.ProductDtos.*;
import com.stockpulse.domain.model.SuggestionStatus;
import com.stockpulse.repository.*;
import com.stockpulse.web.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class SuggestionService {
    private final PricingSuggestionRepository pricing;
    private final ReorderSuggestionRepository reorder;
    private final ProductRepository products;
    public SuggestionService(PricingSuggestionRepository pricing, ReorderSuggestionRepository reorder, ProductRepository products) { this.pricing = pricing; this.reorder = reorder; this.products = products; }
    public List<PricingSuggestionResponse> listPendingPricingSuggestions() { return pricing.findByStatusOrderByCreatedAtDesc(SuggestionStatus.PENDING).stream().map(PricingSuggestionResponse::from).toList(); }
    public List<ReorderSuggestionResponse> listPendingReorderSuggestions() { return reorder.findByStatusOrderByCreatedAtDesc(SuggestionStatus.PENDING).stream().map(ReorderSuggestionResponse::from).toList(); }

    @Transactional public PricingSuggestionResponse decidePricingSuggestion(String id, DecisionRequest request) {
        validateDecision(request);
        var suggestion = pricing.findById(id).orElseThrow(() -> new NotFoundException("Pricing suggestion", id));
        ensurePending(suggestion.getStatus());
        if (request.status() == SuggestionStatus.ACCEPTED) { var product = products.findById(suggestion.getProductId()).orElseThrow(() -> new NotFoundException("Product", suggestion.getProductId())); product.setCurrentPrice(suggestion.getRecommendedPrice()); products.save(product); suggestion.accept(); }
        else suggestion.reject();
        return PricingSuggestionResponse.from(pricing.save(suggestion));
    }

    @Transactional public ReorderSuggestionResponse decideReorderSuggestion(String id, DecisionRequest request) {
        validateDecision(request);
        var suggestion = reorder.findById(id).orElseThrow(() -> new NotFoundException("Reorder suggestion", id));
        ensurePending(suggestion.getStatus());
        if (request.status() == SuggestionStatus.ACCEPTED) { var product = products.findById(suggestion.getProductId()).orElseThrow(() -> new NotFoundException("Product", suggestion.getProductId())); product.setStockLevel(product.getStockLevel() + suggestion.getRecommendedQuantity()); products.save(product); suggestion.accept(); }
        else suggestion.reject();
        return ReorderSuggestionResponse.from(reorder.save(suggestion));
    }

    private void validateDecision(DecisionRequest request) { if (request.status() == SuggestionStatus.PENDING) throw new BusinessException(ApiErrorCodes.INVALID_DECISION, "Decision must be ACCEPTED or REJECTED"); }
    private void ensurePending(SuggestionStatus status) { if (status != SuggestionStatus.PENDING) throw new ConflictException(ApiErrorCodes.SUGGESTION_ALREADY_FINALIZED, "Suggestion is already " + status); }
}
