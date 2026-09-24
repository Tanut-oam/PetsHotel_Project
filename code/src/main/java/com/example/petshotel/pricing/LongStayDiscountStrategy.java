package com.example.petshotel.pricing;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import com.example.petshotel.domain.enums.PricingCategory;

@Component
public class LongStayDiscountStrategy implements PricingStrategy {

    private static final int MINIMUM_NIGHTS = 5;
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");


    @Override
    public BigDecimal calculate(PricingContext context, BigDecimal subtotal) {
        if (context == null || subtotal == null || subtotal.signum() < 0) {
            throw new IllegalArgumentException("Invalid data for long-stay discount");
        }

        if (context.getNights() < MINIMUM_NIGHTS) {
            return BigDecimal.ZERO;
        }
        return subtotal.multiply(DISCOUNT_RATE);
    }

    @Override
    public PricingCategory category() {
        return PricingCategory.DISCOUNT;
    }
}