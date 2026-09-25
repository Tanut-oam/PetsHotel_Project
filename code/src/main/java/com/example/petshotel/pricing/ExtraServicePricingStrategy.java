package com.example.petshotel.pricing;

import java.math.BigDecimal;
import com.example.petshotel.domain.entity.BookingExtraService;
import org.springframework.stereotype.Component;
import com.example.petshotel.domain.enums.PricingCategory;

@Component 
public class ExtraServicePricingStrategy implements PricingStrategy{
    
    @Override 
    public BigDecimal calculate(PricingContext context, BigDecimal subtotal){
        if (context == null) {
            throw new IllegalArgumentException(
                "Pricing context is required"
            );
        }

        if (context.getExtraServices() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalServicePrice = BigDecimal.ZERO;

        for(BookingExtraService item : context.getExtraServices()){
            if(item == null
                || item.getUnitPrice() == null
                || item.getUnitPrice().signum() < 0
                || item.getQuantity() == null
                || item.getQuantity() <= 0
            ){
                throw new IllegalArgumentException(
                "Each extra service needs a non-negative "
                                + "unit price and positive quantity");
            }

            BigDecimal calculated = item.getUnitPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));

            if (item.getTotalPrice() != null
                && item.getTotalPrice().compareTo(calculated) != 0){
                    throw new IllegalArgumentException(
                        "Extra service total does not match "
                                + "unit price multiplied by quantity");
            }
            totalServicePrice = totalServicePrice.add(calculated);
        }

        return totalServicePrice;

    }
    @Override
    public PricingCategory category() {
        return PricingCategory.EXTRA_SERVICE;
    }
}
