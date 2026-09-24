package com.example.petshotel.pricing;

import java.math.BigDecimal;

import com.example.petshotel.domain.enums.PricingCategory;

public interface PricingStrategy {
    BigDecimal calculate(PricingContext context, BigDecimal subtotal);
    PricingCategory category();
}
