package com.example.petshotel.service;

import java.util.List;
import com.example.petshotel.domain.entity.Promotion;
import com.example.petshotel.dto.request.CreatePromotionRequest;

public interface PromotionService {
    List<Promotion> getAllPromotions();
    List<Promotion> getActivePromotions();
    Promotion getPromotionById(Long id);
    Promotion createPromotion(CreatePromotionRequest promotion);
    Promotion updatePromotion(Long id, CreatePromotionRequest request);
    void deactivatePromotion(Long id);
    void validateDiscount(CreatePromotionRequest request);
}
