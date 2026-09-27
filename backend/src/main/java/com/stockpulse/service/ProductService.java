package com.stockpulse.service;

import com.stockpulse.api.ProductDtos.*;
import com.stockpulse.domain.model.*;
import com.stockpulse.repository.ProductRepository;
import com.stockpulse.repository.PricingSuggestionRepository;
import com.stockpulse.repository.ReorderSuggestionRepository;
import com.stockpulse.web.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository products;
    private final PricingSuggestionRepository pricing;
    private final ReorderSuggestionRepository reorder;
    private final ApplicationEventPublisher events;
    public ProductService(ProductRepository products, PricingSuggestionRepository pricing, ReorderSuggestionRepository reorder, ApplicationEventPublisher events) { this.products = products; this.pricing = pricing; this.reorder = reorder; this.events = events; }

    @Transactional public Product create(CreateProductRequest request) {
        if (products.existsBySku(request.sku())) throw new ConflictException(ApiErrorCodes.DUPLICATE_SKU, "Product with SKU " + request.sku() + " already exists");
        return products.save(new Product(request.sku(), request.name(), request.category(), request.currentPrice(), request.stockLevel(), request.reorderThreshold(), request.demandVelocity()));
    }

    public List<Product> findByFilters(String status, String category) {
        try {
            if (status != null && category != null) return products.findByStatusAndCategory(ProductStatus.valueOf(status.toUpperCase()), ProductCategory.valueOf(category.toUpperCase()));
            if (status != null) return products.findByStatus(ProductStatus.valueOf(status.toUpperCase()));
            if (category != null) return products.findByCategory(ProductCategory.valueOf(category.toUpperCase()));
            return products.findAll();
        } catch (IllegalArgumentException ex) { throw new IllegalArgumentException("Invalid product status or category"); }
    }

    public Product findById(String id) { return find(id); }

    @Transactional public Product updateStock(String id, int quantity) {
        Product product = find(id);
        if (quantity < 0) throw new IllegalArgumentException("Stock quantity cannot be negative");
        product.setStockLevel(quantity); products.save(product); checkTriggers(product);
        return product;
    }

    @Transactional public Product createOrder(String id, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Order quantity must be greater than zero");
        Product product = find(id);
        if (product.getStockLevel() < quantity) throw new ConflictException(ApiErrorCodes.STOCK_CONFLICT, "Insufficient stock. Available: " + product.getStockLevel() + ", Requested: " + quantity);
        product.setStockLevel(product.getStockLevel() - quantity);
        product.setDemandVelocity(product.getDemandVelocity() + quantity);
        products.save(product); checkTriggers(product);
        return product;
    }

    @Transactional public void requestPricingSuggestion(String id) { find(id); events.publishEvent(new RecommendationRequested(id, TriggerReason.MANUAL, true, false)); }
    @Transactional public void requestReorderSuggestion(String id) { find(id); events.publishEvent(new RecommendationRequested(id, TriggerReason.MANUAL, false, true)); }

    @Transactional
    public void remove(String id) {
        Product product = find(id);
        pricing.deleteByProductId(product.getId());
        reorder.deleteByProductId(product.getId());
        products.delete(product);
    }

    private Product find(String id) { return products.findById(id).orElseThrow(() -> new NotFoundException("Product", id)); }
    private void checkTriggers(Product product) {
        boolean lowInventory = product.getStockLevel() < product.getReorderThreshold();
        boolean demandSpike = product.getDemandVelocity() > products.averageDemandVelocity(product.getCategory()) * 2;
        if (product.getStockLevel() > 0 && (lowInventory || demandSpike)) product.setStatus(ProductStatus.PRICE_REVIEW_PENDING);
        if (lowInventory) events.publishEvent(new RecommendationRequested(product.getId(), TriggerReason.INVENTORY_LOW));
        if (demandSpike) events.publishEvent(new RecommendationRequested(product.getId(), TriggerReason.DEMAND_SPIKE));
    }
}
