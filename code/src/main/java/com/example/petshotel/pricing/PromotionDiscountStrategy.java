package com.example.petshotel.pricing;

import com.example.petshotel.domain.entity.Promotion;
import com.example.petshotel.domain.enums.PricingCategory;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class PromotionDiscountStrategy implements PricingStrategy {
    
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    @Override
    public BigDecimal calculate(PricingContext context, BigDecimal subtotal) {

        if (context == null || subtotal == null || subtotal.signum() < 0) {
            throw new IllegalArgumentException("Invalid data for promotion discount");
        }

        Promotion promotion = context.getPromotion();

        if (promotion == null ||
                !Boolean.TRUE.equals(promotion.getActive())) {
            return BigDecimal.ZERO;
        }

        if (promotion.getMinimumNights() != null
                && context.getNights() < promotion.getMinimumNights()) {
            return BigDecimal.ZERO;
        }

        if (promotion.getStartDate() != null
                && context.getCheckIn().isBefore(promotion.getStartDate())) {
            return BigDecimal.ZERO;
        }

        if (promotion.getEndDate() != null
                && context.getCheckIn().isAfter(promotion.getEndDate())) {
            return BigDecimal.ZERO;
        }

        if (promotion.getType() == null
                || promotion.getValue() == null
                || promotion.getValue().signum() < 0) {
            throw new IllegalArgumentException("Invalid promotion");
        }

        return switch(promotion.getType()){
            case PERCENTAGE -> subtotal.multiply(promotion.getValue())
                    .divide(ONE_HUNDRED);
            case FIXED_AMOUNT -> promotion.getValue().min(subtotal);
        };
    }

    @Override
    public PricingCategory category() {
        return PricingCategory.DISCOUNT;
    }
}