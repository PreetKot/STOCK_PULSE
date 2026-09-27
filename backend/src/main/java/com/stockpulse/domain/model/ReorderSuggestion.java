package com.stockpulse.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reorder_suggestions")
public class ReorderSuggestion {
    @Id @Column(length = 64, nullable = false, updatable = false) private String id;
    @NotBlank @Column(name = "product_id", length = 64, nullable = false) private String productId;
    @NotNull @Min(0) @Column(name = "current_stock", nullable = false) private Integer currentStock;
    @NotNull @Min(1) @Column(name = "recommended_quantity", nullable = false) private Integer recommendedQuantity;
    @Min(0) @Column(name = "lead_time_days") private Integer leadTimeDays;
    @NotNull @DecimalMin("0.0000") @DecimalMax("1.0000") @Column(precision = 5, scale = 4, nullable = false) private BigDecimal confidence;
    @NotBlank @Column(length = 1000, nullable = false) private String reasoning;
    @Enumerated(EnumType.STRING) @Column(length = 16, nullable = false) private SuggestionStatus status = SuggestionStatus.PENDING;
    @Enumerated(EnumType.STRING) @Column(name = "trigger_reason", length = 32, nullable = false) private TriggerReason triggerReason;
    @NotNull @Column(name = "created_at", nullable = false) private Instant createdAt;
    @NotNull @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    protected ReorderSuggestion() { }
    public ReorderSuggestion(String productId, int currentStock, int recommendedQuantity, Integer leadTimeDays, BigDecimal confidence, String reasoning, SuggestionStatus status, TriggerReason triggerReason) { this.id=UUID.randomUUID().toString(); this.productId=productId; this.currentStock=currentStock; this.recommendedQuantity=recommendedQuantity; this.leadTimeDays=leadTimeDays; this.confidence=confidence; this.reasoning=reasoning; this.status=status; this.triggerReason=triggerReason; this.createdAt=Instant.now(); this.updatedAt=this.createdAt; }
    public ReorderSuggestion(String id, String productId, Integer currentStock, Integer recommendedQuantity, Integer leadTimeDays, BigDecimal confidence, String reasoning, SuggestionStatus status, TriggerReason triggerReason, Instant createdAt, Instant updatedAt) { this.id=id; this.productId=productId; this.currentStock=currentStock; this.recommendedQuantity=recommendedQuantity; this.leadTimeDays=leadTimeDays; this.confidence=confidence; this.reasoning=reasoning; this.status=status; this.triggerReason=triggerReason; this.createdAt=createdAt; this.updatedAt=updatedAt; }
    public void accept(){requirePending();status=SuggestionStatus.ACCEPTED;updatedAt=Instant.now();} public void reject(){requirePending();status=SuggestionStatus.REJECTED;updatedAt=Instant.now();} private void requirePending(){if(status!=SuggestionStatus.PENDING)throw new IllegalStateException("Suggestion is already finalized");}
    public String getId(){return id;} public String getProductId(){return productId;} public Integer getCurrentStock(){return currentStock;} public Integer getRecommendedQuantity(){return recommendedQuantity;} public Integer getLeadTimeDays(){return leadTimeDays;} public BigDecimal getConfidence(){return confidence;} public String getReasoning(){return reasoning;} public SuggestionStatus getStatus(){return status;} public TriggerReason getTriggerReason(){return triggerReason;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
    public boolean isPending(){return status==SuggestionStatus.PENDING;} public boolean isAccepted(){return status==SuggestionStatus.ACCEPTED;} public boolean isRejected(){return status==SuggestionStatus.REJECTED;}
    public void setId(String value){id=value;} public void setProductId(String value){productId=value;} public void setCurrentStock(Integer value){if(value==null||value<0)throw new IllegalArgumentException("Current stock cannot be negative");currentStock=value;} public void setRecommendedQuantity(Integer value){if(value==null||value<1)throw new IllegalArgumentException("Recommended quantity must be greater than zero");recommendedQuantity=value;} public void setConfidence(BigDecimal value){if(value==null||value.compareTo(BigDecimal.ZERO)<0||value.compareTo(BigDecimal.ONE)>0)throw new IllegalArgumentException("Confidence must be between 0 and 1");confidence=value;} public void setReasoning(String value){reasoning=value;} public void setTriggerReason(TriggerReason value){triggerReason=value;} public void setCreatedAt(Instant value){createdAt=value;} public void setUpdatedAt(Instant value){updatedAt=value;} public void setStatus(SuggestionStatus value){if(status!=null&&status!=SuggestionStatus.PENDING)throw new IllegalStateException("Cannot change status of a finalized suggestion");status=value;}
}
