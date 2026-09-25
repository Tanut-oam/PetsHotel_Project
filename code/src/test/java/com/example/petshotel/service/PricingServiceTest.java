package com.example.petshotel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.petshotel.domain.entity.BookingExtraService;
import com.example.petshotel.domain.entity.Promotion;
import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.domain.enums.PromotionType;
import com.example.petshotel.dto.response.BookingPriceResponse;
import com.example.petshotel.pricing.BaseRoomPriceStrategy;
import com.example.petshotel.pricing.ExtraServicePricingStrategy;
import com.example.petshotel.pricing.HolidaySurchargeStrategy;
import com.example.petshotel.pricing.LongStayDiscountStrategy;
import com.example.petshotel.pricing.PricingContext;
import com.example.petshotel.pricing.PromotionDiscountStrategy;
import com.example.petshotel.service.impl.PricingServiceImpl;

public class PricingServiceTest {
    private PricingServiceImpl pricingService;

    @BeforeEach 
    void setUp(){
        pricingService = new PricingServiceImpl(List.of(
            new BaseRoomPriceStrategy(),
            new ExtraServicePricingStrategy(),
            new HolidaySurchargeStrategy(),
            new LongStayDiscountStrategy(),
            new PromotionDiscountStrategy()
        ));
    }

    private PricingContext context(
            String nightlyRate,
            int petCount,
            LocalDate checkIn,
            LocalDate checkOut){
        Room room = new Room();
        room.setPricePerPetPerNight(new BigDecimal(nightlyRate));

        PricingContext context = new PricingContext();
        context.setRoom(room);
        context.setPetCount(petCount);
        context.setCheckIn(checkIn);
        context.setCheckOut(checkOut);
        return context;
    }

    @Test 
    void calculatesRoomAndExtraServicePrices(){
        PricingContext context = context(
            "500.00", 2, 
            LocalDate.of(2026, 2, 1), 
            LocalDate.of(2026, 2, 4));
        
        BookingExtraService extra = new BookingExtraService();
        extra.setUnitPrice(new BigDecimal("200.00"));
        extra.setQuantity(2);
        extra.setTotalPrice(new BigDecimal("400.00"));
        context.setExtraServices(List.of(extra));

        BookingPriceResponse price = pricingService.calculate(context);

        assertEquals(new BigDecimal("3000.00"), price.basePrice());
        assertEquals(new BigDecimal("400.00"), price.extraServicesPrice());
        assertEquals(new BigDecimal("0.00"), price.holidaySurcharge());
        assertEquals(new BigDecimal("0.00"), price.discountAmount());
        assertEquals(new BigDecimal("3400.00"), price.totalPrice());
    }


    @Test 
    void addsSurchargeOnlyForHolidayNights(){
        PricingContext context = context(
            "500", 1, 
            LocalDate.of(2026, 4, 12), 
            LocalDate.of(2026, 4, 15));

        BookingPriceResponse price = pricingService.calculate(context);

        assertEquals(new BigDecimal("1500.00"), price.basePrice());
        assertEquals(new BigDecimal("300.00"), price.holidaySurcharge());
        assertEquals(new BigDecimal("1800.00"), price.totalPrice());
    }

    @Test 
    void choosesPromotionInsteadOfSmallerLongStayDiscount(){
        PricingContext context = context(
            "500.00", 2, 
            LocalDate.of(2026, 2, 1), 
            LocalDate.of(2026, 2, 6));
        
        BookingExtraService extraService = new BookingExtraService();
        extraService.setUnitPrice(new BigDecimal("200.00"));
        extraService.setQuantity(1);
        context.setExtraServices(List.of(extraService));

        Promotion promotion = new Promotion();
        promotion.setType(PromotionType.PERCENTAGE);
        promotion.setValue(new BigDecimal("15"));
        promotion.setActive(true);
        promotion.setStartDate(LocalDate.of(2026, 1, 1));
        promotion.setEndDate(LocalDate.of(2026, 12, 31));
        context.setPromotion(promotion);

        BookingPriceResponse price = pricingService.calculate(context);
        
        assertEquals(new BigDecimal("780.00"), price.discountAmount());
        assertEquals(new BigDecimal("4420.00"), price.totalPrice());
    }

    @Test 
    void roundsDiscountToTwoDecimalPlaces(){
        PricingContext context = context(
            "19.99", 1,
            LocalDate.of(2026, 2, 1),
            LocalDate.of(2026, 2, 2));
        
        Promotion promotion = new Promotion();
        promotion.setType(PromotionType.PERCENTAGE);
        promotion.setValue(new BigDecimal("15"));
        promotion.setActive(true);
        context.setPromotion(promotion);

        BookingPriceResponse price = pricingService.calculate(context);

        assertEquals(new BigDecimal("3.00"), price.discountAmount());
        assertEquals(new BigDecimal("16.99"), price.totalPrice());
    }

    @Test 
    void rejectsCheckoutOnTheSameDay(){
        PricingContext context = context(
           "500.00", 1,
            LocalDate.of(2026, 2, 1),
            LocalDate.of(2026, 2, 1));
        assertThrows(IllegalArgumentException.class, () -> pricingService.calculate(context));

    }


}
