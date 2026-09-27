package com.stockpulse.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.time.Duration;
import java.util.Map;

@Component
public class LLMGateway {
    private final ObjectMapper mapper;
    private final RestClient http;
    private final String provider;
    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final String productHeader;

    public LLMGateway(ObjectMapper mapper,
                      @Value("${llm.provider:openai-compatible}") String provider,
                      @Value("${llm.api-key:}") String apiKey,
                      @Value("${llm.model:qwen-cursor}") String model,
                      @Value("${llm.base-url:http://localhost:4000}") String baseUrl,
                      @Value("${llm.product-header:PC1}") String productHeader) {
        this.mapper = mapper; this.provider = provider; this.apiKey = apiKey; this.model = model; this.baseUrl = baseUrl; this.productHeader = productHeader;
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5)); factory.setReadTimeout(Duration.ofSeconds(20));
        this.http = RestClient.builder().requestFactory(factory).build();
    }

    public boolean configured() { return apiKey != null && !apiKey.isBlank() && !"rule".equalsIgnoreCase(provider); }

    public JsonNode call(String prompt) {
        if (!configured()) throw new IllegalStateException("LLM is not configured");
        try {
            String content = "gemini".equalsIgnoreCase(provider) ? callGemini(prompt) : callOpenAiCompatible(prompt);
            if (content.isBlank()) throw new IllegalStateException("LLM returned no content");
            String json = content.replaceAll("^```json\\s*", "").replaceAll("\\s*```$", "").trim();
            return mapper.readTree(json);
        } catch (java.io.IOException | org.springframework.web.client.RestClientException | IllegalStateException ex) {
            throw new IllegalStateException("LLM request failed", ex);
        }
    }

    private String callGemini(String prompt) throws java.io.IOException {
        String endpoint = baseUrl.replaceAll("/$", "") + "/v1beta/models/" + model + ":generateContent";
        String body = http.post().uri(endpoint).contentType(MediaType.APPLICATION_JSON)
                .header("x-goog-api-key", apiKey)
                .body(Map.of(
                        "contents", java.util.List.of(Map.of("parts", java.util.List.of(Map.of("text", prompt)))),
                        "generationConfig", Map.of("responseMimeType", "application/json", "temperature", 0.1)))
                .retrieve().body(String.class);
        return mapper.readTree(body).path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
    }

    private String callOpenAiCompatible(String prompt) throws java.io.IOException {
        String endpoint = baseUrl.replaceAll("/$", "") + "/v1/chat/completions";
        String body = http.post().uri(endpoint).contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> { headers.setBearerAuth(apiKey); if (productHeader != null && !productHeader.isBlank()) headers.set("product", productHeader); })
                .body(Map.of("model", model, "temperature", 0.1, "messages", java.util.List.of(Map.of("role", "user", "content", prompt))))
                .retrieve().body(String.class);
        return mapper.readTree(body).path("choices").path(0).path("message").path("content").asText();
    }
}
