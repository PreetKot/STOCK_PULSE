package com.stockpulse.commerce;

public record ReorderRecommendation(int recommendedQuantity, int leadTimeDays, java.math.BigDecimal confidence, String reasoning) { }