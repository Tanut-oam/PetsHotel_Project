package com.example.petshotel.pricing;

import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal calculate(PricingContext context);
    
}
