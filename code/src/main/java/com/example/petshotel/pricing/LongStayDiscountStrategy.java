package com.example.petshotel.pricing;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component 
public class LongStayDiscountStrategy implements PricingStrategy{
    private final BaseRoomPriceStrategy baseRoomPriceStrategy;
    
    public LongStayDiscountStrategy(BaseRoomPriceStrategy baseRoomPriceStrategy) {
        this.baseRoomPriceStrategy = baseRoomPriceStrategy;
    }
    
    @Override 
    public BigDecimal calculate(PricingContext context){
        if (context.getNights() >= 3) {
            BigDecimal basePrice = baseRoomPriceStrategy.calculate(context);
            return basePrice.multiply(new BigDecimal("0.10"));
        }   
        return BigDecimal.ZERO;
    }
}
