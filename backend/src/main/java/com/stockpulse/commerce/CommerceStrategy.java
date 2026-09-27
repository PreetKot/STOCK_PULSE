package com.stockpulse.commerce;

public interface CommerceStrategy {
    PricingRecommendation recommendPricing(CommerceContext context);
    ReorderRecommendation recommendReorder(CommerceContext context);
}