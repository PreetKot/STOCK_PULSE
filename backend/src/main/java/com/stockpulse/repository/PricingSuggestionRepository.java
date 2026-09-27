package com.stockpulse.repository;

import com.stockpulse.domain.model.PricingSuggestion;
import com.stockpulse.domain.model.SuggestionStatus;
import com.stockpulse.domain.model.TriggerReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PricingSuggestionRepository extends JpaRepository<PricingSuggestion, String> {

    List<PricingSuggestion> findByProductId(String productId);

    void deleteByProductId(String productId);

    List<PricingSuggestion> findByProductIdAndStatus(String productId, SuggestionStatus status);

    List<PricingSuggestion> findByStatus(SuggestionStatus status);

    List<PricingSuggestion> findByTriggerReason(TriggerReason triggerReason);

    @Query("SELECT ps FROM PricingSuggestion ps WHERE ps.productId = :productId AND ps.triggerReason = :triggerReason AND ps.status = 'PENDING'")
    List<PricingSuggestion> findPendingByProductIdAndTriggerReason(@Param("productId") String productId, @Param("triggerReason") TriggerReason triggerReason);

    @Query("SELECT COUNT(ps) > 0 FROM PricingSuggestion ps WHERE ps.productId = :productId AND ps.triggerReason = :triggerReason AND ps.status = 'PENDING'")
    boolean existsPendingSuggestion(@Param("productId") String productId, @Param("triggerReason") TriggerReason triggerReason);

    List<PricingSuggestion> findByStatusOrderByCreatedAtDesc(SuggestionStatus status);
}