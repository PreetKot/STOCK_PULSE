package com.stockpulse.repository;

import com.stockpulse.domain.model.Product;
import com.stockpulse.domain.model.ProductCategory;
import com.stockpulse.domain.model.ProductStatus;
import com.stockpulse.domain.model.TriggerReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Product> findByStatus(ProductStatus status);

    List<Product> findByCategory(ProductCategory category);

    List<Product> findByStatusAndCategory(ProductStatus status, ProductCategory category);

    @Query("SELECT p FROM Product p WHERE p.stockLevel <= p.reorderThreshold")
    List<Product> findLowStockProducts();

    @Query("SELECT COUNT(ps) > 0 FROM PricingSuggestion ps WHERE ps.productId = :productId AND ps.triggerReason = :triggerReason AND ps.status = 'PENDING'")
    boolean existsPendingPricingSuggestion(@Param("productId") String productId, @Param("triggerReason") TriggerReason triggerReason);

    @Query("SELECT COUNT(rs) > 0 FROM ReorderSuggestion rs WHERE rs.productId = :productId AND rs.triggerReason = :triggerReason AND rs.status = 'PENDING'")
    boolean existsPendingReorderSuggestion(@Param("productId") String productId, @Param("triggerReason") TriggerReason triggerReason);

    @Query("select coalesce(avg(p.demandVelocity), 0) from Product p where p.category = :category")
    double averageDemandVelocity(ProductCategory category);
}