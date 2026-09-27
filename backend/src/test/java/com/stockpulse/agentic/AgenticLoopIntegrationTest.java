package com.stockpulse.agentic;

import com.stockpulse.api.ProductDtos.*;
import com.stockpulse.domain.model.ProductCategory;
import com.stockpulse.domain.model.ProductStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:agentic-loop;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.flyway.locations=classpath:db/migration-h2"
})
class AgenticLoopIntegrationTest {
    @Autowired private TestRestTemplate http;

    @BeforeEach
    void enablePatchRequests() {
        http.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
    }

    @Test
    void orderCreatesPendingSuggestionsAndApprovalChangesPrice() throws InterruptedException {
        String sku = "LOOP-" + UUID.randomUUID();
        var product = new CreateProductRequest(sku, "Agentic loop test", ProductCategory.ELECTRONICS, BigDecimal.TEN, 2, 5, 0);
        ResponseEntity<ProductResponse> created = http.postForEntity("/api/products", product, ProductResponse.class);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        ProductResponse createdProduct = Objects.requireNonNull(created.getBody());
        String id = createdProduct.id();

        ResponseEntity<ProductResponse> order = http.postForEntity("/api/products/" + id + "/orders", new CreateOrderRequest(1), ProductResponse.class);
        assertEquals(HttpStatus.OK, order.getStatusCode());
        ProductResponse orderedProduct = Objects.requireNonNull(order.getBody());
        assertEquals(ProductStatus.PRICE_REVIEW_PENDING, orderedProduct.status());

        List<PricingSuggestionResponse> pricing = List.of();
        List<ReorderSuggestionResponse> reorder = List.of();
        for (int attempt = 0; attempt < 20 && (pricing.isEmpty() || reorder.isEmpty()); attempt++) {
            TimeUnit.MILLISECONDS.sleep(100);
            pricing = pending("/api/pricing-suggestions", new ParameterizedTypeReference<>() {});
            reorder = pending("/api/reorder-suggestions", new ParameterizedTypeReference<>() {});
            pricing = pricing.stream().filter(item -> item.productId().equals(id)).toList();
            reorder = reorder.stream().filter(item -> item.productId().equals(id)).toList();
        }
        assertEquals(1, pricing.size());
        assertEquals(1, reorder.size());
        assertEquals("PENDING", pricing.get(0).status().name());

        ResponseEntity<PricingSuggestionResponse> decision = http.exchange(
                "/api/pricing-suggestions/" + pricing.get(0).id(), HttpMethod.PATCH,
                new HttpEntity<>(new DecisionRequest(com.stockpulse.domain.model.SuggestionStatus.ACCEPTED)), PricingSuggestionResponse.class);
        assertEquals(HttpStatus.OK, decision.getStatusCode());
        assertEquals(new BigDecimal("11.00"), http.getForObject("/api/products/" + id, ProductResponse.class).currentPrice());
    }

    private <T> List<T> pending(String path, ParameterizedTypeReference<List<T>> type) {
        return http.exchange(path, HttpMethod.GET, HttpEntity.EMPTY, type).getBody();
    }
}
