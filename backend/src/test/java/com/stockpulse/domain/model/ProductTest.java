package com.stockpulse.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void shouldCreateProductWithValidParameters() {
        // Given
        String id = "test-product-1";
        String sku = "TEST-SKU-001";
        String name = "Test Product";
        ProductCategory category = ProductCategory.ELECTRONICS;
        BigDecimal currentPrice = new BigDecimal("99.99");
        Integer stockLevel = 10;
        Integer reorderThreshold = 5;
        Integer demandVelocity = 3;
        ProductStatus status = ProductStatus.ACTIVE;
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();

        // When
        Product product = new Product(id, sku, name, category, currentPrice, stockLevel,
                                    reorderThreshold, demandVelocity, status, createdAt, updatedAt);

        // Then
        assertNotNull(product);
        assertEquals(id, product.getId());
        assertEquals(sku, product.getSku());
        assertEquals(name, product.getName());
        assertEquals(category, product.getCategory());
        assertEquals(currentPrice, product.getCurrentPrice());
        assertEquals(stockLevel, product.getStockLevel());
        assertEquals(reorderThreshold, product.getReorderThreshold());
        assertEquals(demandVelocity, product.getDemandVelocity());
        assertEquals(status, product.getStatus());
        assertEquals(createdAt, product.getCreatedAt());
        assertEquals(updatedAt, product.getUpdatedAt());
    }

    @Test
    void shouldSetOutOfStockStatusWhenStockLevelIsZero() {
        // Given
        Product product = new Product();
        product.setStockLevel(5);
        product.setStatus(ProductStatus.ACTIVE);

        // When
        product.setStockLevel(0);

        // Then
        assertEquals(ProductStatus.OUT_OF_STOCK, product.getStatus());
    }

    @Test
    void shouldAllowPositiveStockLevel() {
        // Given
        Product product = new Product();

        // When
        product.setStockLevel(10);

        // Then
        assertEquals(10, product.getStockLevel());
    }

    @Test
    void shouldAllowZeroStockLevel() {
        // Given
        Product product = new Product();

        // When
        product.setStockLevel(0);

        // Then
        assertEquals(0, product.getStockLevel());
        assertEquals(ProductStatus.OUT_OF_STOCK, product.getStatus());
    }

    @Test
    void shouldDefaultToUnspecifiedCategoryWhenNull() {
        // Given
        Product product = new Product();

        // When
        product.setCategory(null);

        // Then
        assertEquals(ProductCategory.UNSPECIFIED, product.getCategory());
    }
}
