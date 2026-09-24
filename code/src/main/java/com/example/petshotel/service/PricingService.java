package com.example.petshotel.service;

import com.example.petshotel.dto.response.BookingPriceResponse;
import com.example.petshotel.pricing.PricingContext;

public interface PricingService {
    BookingPriceResponse calculate(PricingContext input);
}
