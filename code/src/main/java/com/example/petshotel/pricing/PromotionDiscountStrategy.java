package com.example.petshotel.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.example.petshotel.domain.entity.Promotion;
import com.example.petshotel.domain.enums.PromotionType;

@Component 
public class PromotionDiscountStrategy implements PricingStrategy{
    private final BaseRoomPriceStrategy baseRoomPriceStrategy;

    public PromotionDiscountStrategy(BaseRoomPriceStrategy baseRoomPriceStrategy){
        this.baseRoomPriceStrategy = baseRoomPriceStrategy;
    }
    
    @Override 
    public BigDecimal calculate(PricingContext context){
        
        Promotion promotion = context.getPromotion();
        
        if (promotion == null || !Boolean.TRUE.equals(promotion.getActive()) || promotion.getValue() == null) {
            return BigDecimal.ZERO;
        }

        if (promotion.getMinimumNights() != null && context.getNights() < promotion.getMinimumNights()) {
            return BigDecimal.ZERO;
        }

        if(context.getCheckIn() != null){
            if(promotion.getStartDate() != null && context.getCheckIn().isBefore(promotion.getStartDate())){
                return BigDecimal.ZERO;
            }

            if (promotion.getEndDate() != null && context.getCheckIn().isAfter(promotion.getEndDate())) {
                return BigDecimal.ZERO;
            }
        }

        if (promotion.getType() == PromotionType.PERCENTAGE) {
            BigDecimal basePrice = baseRoomPriceStrategy.calculate(context);

            BigDecimal discountRate = promotion.getValue().divide(new BigDecimal("100"), 4 , RoundingMode.HALF_UP);
            return basePrice.multiply(discountRate);

        }else if(promotion.getType() == PromotionType.FIXED_AMOUNT){
            return promotion.getValue();
        }
        return BigDecimal.ZERO;
    }
}
