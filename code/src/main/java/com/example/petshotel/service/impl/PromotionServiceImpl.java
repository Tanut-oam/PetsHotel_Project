package com.example.petshotel.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petshotel.domain.entity.Promotion;
import com.example.petshotel.dto.request.CreatePromotionRequest;
import com.example.petshotel.repository.PromotionRepository;
import com.example.petshotel.service.PromotionService;
import java.math.BigDecimal;
import com.example.petshotel.domain.enums.PromotionType;

@Service
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;

    public PromotionServiceImpl(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }


    @Override
    @Transactional(readOnly = true)
    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Promotion getPromotionById(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Promotion not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Promotion> getActivePromotions() {
        LocalDate today = LocalDate.now();
        return promotionRepository.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .filter(p -> p.getStartDate() != null && !today.isBefore(p.getStartDate()))
                .filter(p -> p.getEndDate() != null && !today.isAfter(p.getEndDate()))
                .toList();
    }

    @Override
    @Transactional
    public Promotion createPromotion(CreatePromotionRequest request) {
        validateDiscount(request);
        Promotion promotion = new Promotion();
        promotion.setName(request.name().trim());
        promotion.setType(request.type());
        promotion.setValue(request.discountValue());
        promotion.setStartDate(request.startDate());
        promotion.setEndDate(request.endDate());
        promotion.setActive(request.active());

        return promotionRepository.save(promotion);
    }

    @Override
    @Transactional
    public Promotion updatePromotion(Long id, CreatePromotionRequest request) {
        validateDiscount(request);
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Promotion not found: " + id));

        promotion.setName(request.name().trim());
        promotion.setType(request.type());
        promotion.setValue(request.discountValue());
        promotion.setStartDate(request.startDate());
        promotion.setEndDate(request.endDate());
        promotion.setActive(request.active());

        return promotionRepository.save(promotion);
    }

    @Override
    @Transactional
    public void deactivatePromotion(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Promotion not found: " + id));

        promotion.setActive(false);
        promotionRepository.save(promotion);
    }

    @Override 
    public void validateDiscount(CreatePromotionRequest request) {
        if(request == null
            || request.type() == null
            || request.discountValue() == null
            || request.discountValue().signum() <= 0){
                throw new IllegalArgumentException("Invalid promotion discount");
            }

        if (request.type() == PromotionType.PERCENTAGE && 
                 request.discountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException(
                "Percentage discount must not exceed 100"
            );
        } 
        
        if (request.startDate() == null || request.endDate() == null) {
            throw new IllegalArgumentException(
                "Promotion start date and end date are required"
            );
        }

        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException(
                "Promotion end date must not be before start date"
            );
        }

        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Promotion name is required");
        }
        
    }

}