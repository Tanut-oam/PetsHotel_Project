package com.example.petshotel.service;

import java.util.List;
import java.util.Optional;
import com.example.petshotel.domain.entity.Promotion;

public interface PromotionService {
    List<Promotion> getAllPromotions();
    List<Promotion> getActivePromotions();
    Optional<Promotion> getPromotionById(Long id);
    Promotion createPromotion(Promotion promotion);
    Promotion updatePromotion(Long id, Promotion promotionDetails);
    void deletePromotion(Long id);
}
