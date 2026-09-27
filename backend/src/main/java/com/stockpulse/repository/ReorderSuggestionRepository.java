package com.stockpulse.repository;

import com.stockpulse.domain.model.ReorderSuggestion;
import com.stockpulse.domain.model.SuggestionStatus;
import com.stockpulse.domain.model.TriggerReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReorderSuggestionRepository extends JpaRepository<ReorderSuggestion, String> {

    List<ReorderSuggestion> findByProductId(String productId);

    void deleteByProductId(String productId);

    List<ReorderSuggestion> findByProductIdAndStatus(String productId, SuggestionStatus status);

    List<ReorderSuggestion> findByStatus(SuggestionStatus status);

    List<ReorderSuggestion> findByTriggerReason(TriggerReason triggerReason);

    @Query("SELECT rs FROM ReorderSuggestion rs WHERE rs.productId = :productId AND rs.triggerReason = :triggerReason AND rs.status = 'PENDING'")
    List<ReorderSuggestion> findPendingByProductIdAndTriggerReason(@Param("productId") String productId, @Param("triggerReason") TriggerReason triggerReason);

    @Query("SELECT COUNT(rs) > 0 FROM ReorderSuggestion rs WHERE rs.productId = :productId AND rs.triggerReason = :triggerReason AND rs.status = 'PENDING'")
    boolean existsPendingSuggestion(@Param("productId") String productId, @Param("triggerReason") TriggerReason triggerReason);

    List<ReorderSuggestion> findByStatusOrderByCreatedAtDesc(SuggestionStatus status);
}