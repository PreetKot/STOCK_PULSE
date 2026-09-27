# ADR: StockPulse Commerce Advisor Architecture

## Status

Accepted

## 1. Commerce logic boundary

### Context

Pricing rules, reorder calculations, trigger detection, and persistence have different reasons to change. Putting them in one controller or product entity would make the approval workflow difficult to test and extend.

### Options

1. Put commerce decisions in `Product` or controllers.
2. Keep decisions behind `CommerceStrategy` and orchestrate persistence in services.

### Decision

Use `CommerceStrategy` for pricing and reorder recommendations. `ProductService` owns product writes and trigger publication; `RecommendationService` owns suggestion persistence; `SuggestionService` owns approval transactions.

### Tradeoffs

There are more small classes and interfaces, but HTTP and async callers share the same contract and future strategies do not require controller changes.

## 2. Unified strategy contract

### Context

Pricing and reorder recommendations are created together for every trigger, but they have different output validation and fallback rules.

### Options

1. One AI call returning one combined response.
2. One unified application contract with independently validated pricing and reorder operations.

### Decision

Use one `CommerceStrategy` contract with `recommendPricing` and `recommendReorder` methods. The current AI implementation uses the same structured product context and distinct trigger-aware prompts for each operation; each result can independently fall back to the rule strategy.

### Tradeoffs

Two model calls may cost more than one combined call, but malformed reorder output cannot invalidate an otherwise valid price recommendation, and both operations remain easy to test.

## 3. Runtime strategy selection

### Context

The hackathon requires rule-based behavior immediately and an AI implementation without changing callers.

### Options

1. Hard-code one implementation.
2. Select the implementation through configuration while exposing the same Spring bean contract.

### Decision

`COMMERCE_STRATEGY=rule` is the default and uses deterministic rules. `COMMERCE_STRATEGY=ai` enables `AiCommerceStrategy`; missing credentials or any AI failure falls back to `RuleBasedCommerceStrategy`. The HTTP and event paths inject only `CommerceStrategy`.

### Tradeoffs

The selector adds configuration and fallback branches, but demos work offline and a future `CompetitorAwareStrategy` can implement the same contract.

## 4. LLM failure handling

### Context

Timeouts, unavailable gateways, malformed JSON, missing fields, and unrealistic prices must never silently drop an async recommendation.

### Options

1. Propagate the error and lose the suggestion.
2. Validate the AI response and use deterministic fallback output on every failure.

### Decision

`LLMGateway` reads `LLM_API_KEY`, base URL, and model from environment-backed properties. It uses bounded HTTP timeouts, never logs credentials, parses JSON-only model content, validates confidence, price, direction, reasoning, and reorder quantity, and delegates to the rule strategy when validation or transport fails.

### Tradeoffs

A fallback can be less context-aware than AI, but it preserves the human review queue and guarantees a explainable recommendation.

## 5. Agentic trigger decoupling

### Context

Stock and order requests must return promptly while recommendations are generated after the product write is durable.

### Options

1. Generate recommendations synchronously in the request.
2. Publish an application event and process it asynchronously after commit.

### Decision

Product writes publish `RecommendationRequested`. `@TransactionalEventListener(AFTER_COMMIT)` dispatches to an `@Async` handler. The handler uses the same `CommerceStrategy` contract as manual requests. Pending suggestions are deduplicated by product, trigger reason, and type.

### Tradeoffs

The request path is fast and cannot expose uncommitted state, but async work needs operational monitoring and a retry/dead-letter mechanism in a later sprint.

## 6. Extensibility and exclusions

### Context

The brief reserves margin, competitor, supplier, storefront, authentication, SSE, and automated purchasing work for later sprints.

### Decision

The domain keeps strategy and recommendation boundaries open for `costPrice`, `marginFloor`, `supplierId`, `competitorPrice`, and `priceCooldownMinutes`. A future competitor-aware strategy can plug into `CommerceStrategy`; margin validation can be added at the AI response validation boundary. The current build deliberately excludes authentication, competitor scraping, payment, customer storefront, automated purchase orders, charts, and SSE to protect the inventory-signal to human-approval path.

### Consequences

The current application is smaller and demoable in one flow. Those deferred features will require new persistence, security, supplier, or streaming decisions rather than being hidden inside the current core services.
