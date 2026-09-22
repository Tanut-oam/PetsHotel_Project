package com.example.petshotel.pricing;

import java.time.LocalDate;
import java.util.List;

import com.example.petshotel.domain.entity.BookingExtraService;
import com.example.petshotel.domain.entity.Promotion;
import com.example.petshotel.domain.entity.Room;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class PricingContext {
    private Room room;
    private int petCount;
    private int nights;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private List<BookingExtraService> extraServices;
    private Promotion promotion;
}
