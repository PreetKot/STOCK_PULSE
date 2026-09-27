package com.stockpulse.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {
    @Id @Column(length = 64, nullable = false, updatable = false) private String id;
    @NotBlank @Column(length = 128, nullable = false, unique = true) private String sku;
    @NotBlank @Column(length = 255, nullable = false) private String name;
    @Enumerated(EnumType.STRING) @Column(length = 32, nullable = false) private ProductCategory category = ProductCategory.UNSPECIFIED;
    @NotNull @DecimalMin("0.01") @Column(name = "current_price", precision = 12, scale = 2, nullable = false) private BigDecimal currentPrice;
    @NotNull @Min(0) @Column(name = "stock_level", nullable = false) private Integer stockLevel;
    @NotNull @Min(0) @Column(name = "reorder_threshold", nullable = false) private Integer reorderThreshold;
    @NotNull @Min(0) @Column(name = "demand_velocity", nullable = false) private Integer demandVelocity;
    @Enumerated(EnumType.STRING) @Column(length = 32, nullable = false) private ProductStatus status;
    @NotNull @Column(name = "created_at", nullable = false) private Instant createdAt;
    @NotNull @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected Product() { }
    public Product(String sku, String name, ProductCategory category, BigDecimal currentPrice, int stockLevel, int reorderThreshold, int demandVelocity) {
        this.id = UUID.randomUUID().toString(); this.sku = sku; this.name = name;
        this.category = category == null ? ProductCategory.UNSPECIFIED : category; this.currentPrice = currentPrice;
        this.stockLevel = stockLevel; this.reorderThreshold = reorderThreshold; this.demandVelocity = demandVelocity;
        this.status = stockLevel == 0 ? ProductStatus.OUT_OF_STOCK : ProductStatus.ACTIVE;
        this.createdAt = Instant.now(); this.updatedAt = this.createdAt;
    }
    public Product(String id, String sku, String name, ProductCategory category, BigDecimal currentPrice, Integer stockLevel, Integer reorderThreshold, Integer demandVelocity, ProductStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id; this.sku = sku; this.name = name; this.category = category == null ? ProductCategory.UNSPECIFIED : category; this.currentPrice = currentPrice; this.stockLevel = stockLevel; this.reorderThreshold = reorderThreshold; this.demandVelocity = demandVelocity; this.status = status; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }
    @SuppressWarnings("unused") @PrePersist void beforeInsert() { if (id == null) id = UUID.randomUUID().toString(); if (createdAt == null) createdAt = Instant.now(); updatedAt = createdAt; }
    @SuppressWarnings("unused") @PreUpdate void beforeUpdate() { updatedAt = Instant.now(); }
    public void setStockLevel(Integer value) { if (value == null || value < 0) throw new IllegalArgumentException("Stock cannot be negative"); stockLevel = value; status = value == 0 ? ProductStatus.OUT_OF_STOCK : ProductStatus.ACTIVE; }
    public void setCurrentPrice(BigDecimal value) { if (value == null || value.signum() <= 0) throw new IllegalArgumentException("Price must be greater than zero"); currentPrice = value; }
    public void setDemandVelocity(Integer value) { if (value == null || value < 0) throw new IllegalArgumentException("Demand velocity cannot be negative"); demandVelocity = value; }
    public String getId(){return id;} public void setId(String value){id=value;} public String getSku(){return sku;} public void setSku(String value){sku=value;} public String getName(){return name;} public void setName(String value){name=value;}
    public ProductCategory getCategory(){return category;} public void setCategory(ProductCategory value){category=value==null?ProductCategory.UNSPECIFIED:value;} public BigDecimal getCurrentPrice(){return currentPrice;} public Integer getStockLevel(){return stockLevel;} public Integer getReorderThreshold(){return reorderThreshold;} public void setReorderThreshold(Integer value){reorderThreshold=value;} public Integer getDemandVelocity(){return demandVelocity;} public ProductStatus getStatus(){return status;} public void setStatus(ProductStatus value){status=value;} public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant value){createdAt=value;} public Instant getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Instant value){updatedAt=value;}
}
