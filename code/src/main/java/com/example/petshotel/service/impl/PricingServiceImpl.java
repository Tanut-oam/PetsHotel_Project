package com.example.petshotel.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;

import org.springframework.stereotype.Service;
import com.example.petshotel.domain.enums.PricingCategory;
import com.example.petshotel.dto.response.BookingPriceResponse;
import com.example.petshotel.pricing.PricingContext;
import com.example.petshotel.pricing.PricingStrategy;
import com.example.petshotel.service.PricingService;

@Service
public class PricingServiceImpl implements PricingService {

    private final List<PricingStrategy> strategies;

    public PricingServiceImpl(List<PricingStrategy> strategies) {
        this.strategies = strategies;
    }

    @Override
    public BookingPriceResponse calculate(PricingContext input) {
        if (input == null
            || input.getRoom() == null
            || input.getCheckIn() == null
            || input.getCheckOut() == null
            || input.getPetCount() <= 0
        ) {
            throw new IllegalArgumentException(
                "Invalid pricing data"
            );
        }

        long nights = ChronoUnit.DAYS.between(input.getCheckIn(), input.getCheckOut());
        if (nights <= 0 || nights > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                "Check-out must be after check-in"
            );
        }

        PricingContext context = new PricingContext();
        context.setRoom(input.getRoom());
        context.setPetCount(input.getPetCount());
        context.setNights(Math.toIntExact(nights));
        context.setCheckIn(input.getCheckIn());
        context.setCheckOut(input.getCheckOut());
        context.setExtraServices(input.getExtraServices());
        context.setPromotion(input.getPromotion());

        EnumMap<PricingCategory, BigDecimal> amounts = new EnumMap<>(PricingCategory.class);

        for(PricingStrategy strategy : strategies){
            if (strategy.category() != PricingCategory.DISCOUNT) {
                BigDecimal amount = strategy.calculate(context, BigDecimal.ZERO);
                amounts.merge(strategy.category(), amount, BigDecimal::add);
            }
        }

        BigDecimal room = amounts.getOrDefault(
                PricingCategory.ROOM, BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal extra = amounts.getOrDefault(
                PricingCategory.EXTRA_SERVICE, BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal holiday = amounts.getOrDefault(
                PricingCategory.HOLIDAY_SURCHARGE, BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal subtotal = room.add(extra).add(holiday);

        BigDecimal discount = BigDecimal.ZERO;
        for(PricingStrategy strategy : strategies){
            if (strategy.category() == PricingCategory.DISCOUNT) {
                discount = discount.max(strategy.calculate(context, subtotal));
            }
        }

        discount = discount.setScale(2, RoundingMode.HALF_UP).min(subtotal);
        BigDecimal total = subtotal.subtract(discount);

        return new BookingPriceResponse(
            room, extra, holiday, discount, total
        );

    }

}