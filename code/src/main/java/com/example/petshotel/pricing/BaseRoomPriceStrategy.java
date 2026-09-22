package com.example.petshotel.pricing;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component 
public class BaseRoomPriceStrategy implements PricingStrategy{
    @Override 
    public BigDecimal calculate(PricingContext context){
        if(context.getRoom() == null || context.getRoom().getPricePerPetPerNight() == null){
            return BigDecimal.ZERO;
        }
        return context.getRoom().getPricePerPetPerNight()
                .multiply(BigDecimal.valueOf(context.getNights()))
                .multiply(BigDecimal.valueOf(context.getPetCount())); 
    }
}
