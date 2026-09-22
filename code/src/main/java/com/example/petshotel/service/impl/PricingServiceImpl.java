package com.example.petshotel.service.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.example.petshotel.pricing.BaseRoomPriceStrategy;
import com.example.petshotel.pricing.ExtraServicePricingStrategy;
import com.example.petshotel.pricing.HolidaySurchargeStrategy;
import com.example.petshotel.pricing.LongStayDiscountStrategy;
import com.example.petshotel.pricing.PricingContext;
import com.example.petshotel.pricing.PromotionDiscountStrategy;
import com.example.petshotel.service.PricingService;

@Service 
public class PricingServiceImpl implements PricingService{
    private final BaseRoomPriceStrategy baseRoomPriceStrategy;
    private final HolidaySurchargeStrategy holidaySurchargeStrategy;
    private final ExtraServicePricingStrategy extraServicePricingStrategy;
    private final LongStayDiscountStrategy longStayDiscountStrategy;
    private final PromotionDiscountStrategy promotionDiscountStrategy;

    public PricingServiceImpl(
            BaseRoomPriceStrategy baseRoomPriceStrategy,
            HolidaySurchargeStrategy holidaySurchargeStrategy,
            ExtraServicePricingStrategy extraServicePricingStrategy,
            LongStayDiscountStrategy longStayDiscountStrategy,
            PromotionDiscountStrategy promotionDiscountStrategy) {
        this.baseRoomPriceStrategy = baseRoomPriceStrategy;
        this.holidaySurchargeStrategy = holidaySurchargeStrategy;
        this.extraServicePricingStrategy = extraServicePricingStrategy;
        this.longStayDiscountStrategy = longStayDiscountStrategy;
        this.promotionDiscountStrategy = promotionDiscountStrategy;
    }

    @Override 
    public BigDecimal calculateTotalPrice(PricingContext context){
        if (context == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal basePrice = baseRoomPriceStrategy.calculate(context);
        BigDecimal holidaySurcharge = holidaySurchargeStrategy.calculate(context);
        BigDecimal extraServices = extraServicePricingStrategy.calculate(context);

        BigDecimal subTotal = basePrice.add(holidaySurcharge).add(extraServices);

        BigDecimal longStayDiscount = longStayDiscountStrategy.calculate(context);
        BigDecimal promotionDiscount = promotionDiscountStrategy.calculate(context);

        BigDecimal totalDiscount = longStayDiscount.add(promotionDiscount);

        BigDecimal grandTotal = subTotal.subtract(totalDiscount);

        return grandTotal.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : grandTotal;
    }
}
