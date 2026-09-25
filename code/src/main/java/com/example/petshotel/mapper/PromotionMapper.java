package com.example.petshotel.mapper;

import org.springframework.stereotype.Component;

import com.example.petshotel.domain.entity.Promotion;
import com.example.petshotel.dto.response.PromotionResponse;

@Component 
public class PromotionMapper {
    
    public PromotionResponse toResponse(Promotion promotion){
        return new PromotionResponse(
            promotion.getId(),
            promotion.getName(),
            promotion.getDescription(),
            promotion.getType(),
            promotion.getValue(),
            promotion.getStartDate(),
            promotion.getEndDate(),
            promotion.getMinimumNights(),
            promotion.getActive()
        );
    }

}
