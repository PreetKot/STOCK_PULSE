package com.stockpulse.api;

import com.stockpulse.domain.model.ProductCategory;
import com.stockpulse.domain.model.ProductStatus;
import com.stockpulse.domain.model.PricingDirection;
import com.stockpulse.domain.model.SuggestionStatus;
import com.stockpulse.domain.model.TriggerReason;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public final class ProductDtos {
    private ProductDtos() { }

    public record CreateProductRequest(
        @NotBlank(message = "SKU is required")
        @Size(max = 128, message = "SKU must not exceed 128 characters")
        String sku,

        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String name,

        ProductCategory category,

        @NotNull(message = "Current price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        BigDecimal currentPrice,

        @Min(value = 0, message = "Stock level cannot be negative")
        int stockLevel,

        @Min(value = 0, message = "Reorder threshold cannot be negative")
        int reorderThreshold,

        @Min(value = 0, message = "Demand velocity cannot be negative")
        int demandVelocity
    ) {
        public CreateProductRequest {
            if (category == null) {
                category = ProductCategory.UNSPECIFIED;
            }
        }
    }

    public record UpdateStockRequest(
        @Min(value = 0, message = "Quantity cannot be negative")
        int quantity
    ) { }

    public record CreateOrderRequest(
        @Min(value = 1, message = "Order quantity must be at least 1")
        int quantity
    ) { }

    public record ProductResponse(
        String id,
        String sku,
        String name,
        ProductCategory category,
        BigDecimal currentPrice,
        int stockLevel,
        int reorderThreshold,
        int demandVelocity,
        ProductStatus status,
        Instant createdAt,
        Instant updatedAt
    ) {
        public static ProductResponse from(com.stockpulse.domain.model.Product product) {
            return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getCategory(),
                product.getCurrentPrice(),
                product.getStockLevel(),
                product.getReorderThreshold(),
                product.getDemandVelocity(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
            );
        }
    }

    public record DecisionRequest(
        @NotNull(message = "Status is required")
        SuggestionStatus status
    ) {
        public DecisionRequest {
            if (status == SuggestionStatus.PENDING) {
                throw new IllegalArgumentException("Decision must be ACCEPTED or REJECTED");
            }
        }
    }

    public record PricingSuggestionResponse(
        String id,
        String productId,
        BigDecimal currentPrice,
        BigDecimal recommendedPrice,
        PricingDirection direction,
        BigDecimal confidence,
        String reasoning,
        SuggestionStatus status,
        TriggerReason triggerReason,
        Instant createdAt
    ) {
        public static PricingSuggestionResponse from(com.stockpulse.domain.model.PricingSuggestion s) {
            return new PricingSuggestionResponse(
                s.getId(),
                s.getProductId(),
                s.getCurrentPrice(),
                s.getRecommendedPrice(),
                s.getDirection(),
                s.getConfidence(),
                s.getReasoning(),
                s.getStatus(),
                s.getTriggerReason(),
                s.getCreatedAt()
            );
        }
    }

    public record ReorderSuggestionResponse(
        String id,
        String productId,
        int currentStock,
        int recommendedQuantity,
        Integer leadTimeDays,
        BigDecimal confidence,
        String reasoning,
        SuggestionStatus status,
        TriggerReason triggerReason,
        Instant createdAt
    ) {
        public static ReorderSuggestionResponse from(com.stockpulse.domain.model.ReorderSuggestion s) {
            return new ReorderSuggestionResponse(
                s.getId(),
                s.getProductId(),
                s.getCurrentStock(),
                s.getRecommendedQuantity(),
                s.getLeadTimeDays(),
                s.getConfidence(),
                s.getReasoning(),
                s.getStatus(),
                s.getTriggerReason(),
                s.getCreatedAt()
            );
        }
    }
}