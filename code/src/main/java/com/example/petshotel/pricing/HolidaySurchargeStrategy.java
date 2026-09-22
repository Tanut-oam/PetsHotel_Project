package com.example.petshotel.pricing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import org.springframework.stereotype.Component;

@Component 
public class HolidaySurchargeStrategy implements PricingStrategy{
    private static final BigDecimal SURCHARGE_RATE = new BigDecimal("0.30");

    @Override 
    public BigDecimal calculate(PricingContext context){
        if (context.getRoom() == null || 
            context.getRoom().getPricePerPetPerNight() == null ||
            context.getCheckIn() == null ||
            context.getNights() <= 0) {
            return BigDecimal.ZERO;
        }

        int holidayCount = 0;
        LocalDate currentDate = context.getCheckIn();

        for(int i = 0; i < context.getNights(); i++){
            if(isHoliday(currentDate)){
                holidayCount++;
            }
            currentDate = currentDate.plusDays(1);
        }

        if(holidayCount == 0){
            return BigDecimal.ZERO;
        }

        BigDecimal basePerNight = context.getRoom().getPricePerPetPerNight()
                .multiply(BigDecimal.valueOf(context.getPetCount()));

        BigDecimal surchargePerNight = basePerNight.multiply(SURCHARGE_RATE);
        
        return surchargePerNight.multiply(BigDecimal.valueOf(holidayCount));

    }

    private boolean isHoliday(LocalDate date){
        Month month = date.getMonth();
        int day = date.getDayOfMonth();

        if(month == Month.JANUARY && day == 1) return true;
        if(month == Month.APRIL && (day >= 13 && day <= 15)) return true;
        if(month == Month.DECEMBER && day == 31) return true;

        return false;
    }
}
