package com.example.petshotel.pricing;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

import com.example.petshotel.domain.enums.PricingCategory;

@Component
public class BaseRoomPriceStrategy implements PricingStrategy {
    @Override
    public BigDecimal calculate(PricingContext context, BigDecimal subtotal) {
        if (context == null
                || context.getRoom() == null
                || context.getRoom().getPricePerPetPerNight() == null
                || context.getRoom().getPricePerPetPerNight().signum() < 0
                || context.getNights() <= 0
                || context.getPetCount() <= 0) {
            throw new IllegalArgumentException("Invalid booking data for pricing");
        }

        return context.getRoom().getPricePerPetPerNight()
                .multiply(BigDecimal.valueOf(context.getNights()))
                .multiply(BigDecimal.valueOf(context.getPetCount()));
    }

    @Override
    public PricingCategory category() {
        return PricingCategory.ROOM;
    }
}