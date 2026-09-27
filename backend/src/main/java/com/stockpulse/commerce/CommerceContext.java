package com.stockpulse.commerce;

import com.stockpulse.domain.model.Product;
import com.stockpulse.domain.model.TriggerReason;

public record CommerceContext(Product product, double categoryAverageVelocity, TriggerReason triggerReason) { }