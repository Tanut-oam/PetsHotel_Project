package com.example.petshotel.service;

import java.math.BigDecimal;
import com.example.petshotel.pricing.PricingContext;

public interface PricingService {
    BigDecimal calculateTotalPrice(PricingContext context);
}
