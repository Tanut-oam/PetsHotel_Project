package com.example.petshotel.pricing;

import java.math.BigDecimal;
import com.example.petshotel.domain.entity.BookingExtraService;
import org.springframework.stereotype.Component;

@Component 
public class ExtraServicePricingStrategy implements PricingStrategy{
    @Override 
    public BigDecimal calculate(PricingContext context){
        if (context.getExtraServices() == null || context.getExtraServices().isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalServicePrice = BigDecimal.ZERO;

        for(BookingExtraService item : context.getExtraServices()){
            if(item != null){
                BigDecimal itemTotal = item.getTotalPrice();

                if(itemTotal == null && item.getUnitPrice() != null && item.getQuantity() != null){
                    itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                }

                if(itemTotal != null){
                    totalServicePrice = totalServicePrice.add(itemTotal);
                }

            }
        }

        return totalServicePrice;

    }
}
