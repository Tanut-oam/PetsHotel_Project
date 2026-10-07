package com.example.petshotel.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petshotel.domain.entity.Promotion;
import com.example.petshotel.domain.enums.PromotionType;
import com.example.petshotel.dto.request.CreatePromotionRequest;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.repository.PromotionRepository;
import com.example.petshotel.service.PromotionService;

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
                .orElseThrow(() ->
                        new ResourceNotFoundException("Promotion", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Promotion> getActivePromotions() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Bangkok"));

        return promotionRepository.findAll().stream()
                .filter(promotion -> promotion.isAvailableOn(today))
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
        promotion.setDescription(normalizeDescription(request.description()));
        promotion.setMinimumNights(request.minimumNights());

        return promotionRepository.save(promotion);
    }

    @Override
    @Transactional
    public Promotion updatePromotion(
            Long id,
            CreatePromotionRequest request) {

        validateDiscount(request);

        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Promotion", id));

        promotion.setName(request.name().trim());
        promotion.setType(request.type());
        promotion.setValue(request.discountValue());
        promotion.setStartDate(request.startDate());
        promotion.setEndDate(request.endDate());
        promotion.setActive(request.active());
        promotion.setDescription(normalizeDescription(request.description()));
        promotion.setMinimumNights(request.minimumNights());

        return promotionRepository.save(promotion);
    }

    @Override
    @Transactional
    public void deactivatePromotion(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Promotion", id));

        promotion.setActive(false);
        promotionRepository.save(promotion);
    }

    @Override
    public void validateDiscount(CreatePromotionRequest request) {
        if (request == null
                || request.type() == null
                || request.discountValue() == null
                || request.discountValue().signum() <= 0) {

            throw new IllegalArgumentException("Invalid promotion discount");
        }

        if (request.type() == PromotionType.PERCENTAGE
                && request.discountValue()
                        .compareTo(BigDecimal.valueOf(100)) > 0) {

            throw new IllegalArgumentException(
                    "Percentage discount must not exceed 100");
        }

        if (request.startDate() == null || request.endDate() == null) {
            throw new IllegalArgumentException(
                    "Promotion start date and end date are required");
        }

        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException(
                    "Promotion end date must not be before start date");
        }

        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException(
                    "Promotion name is required");
        }

        if (request.minimumNights() != null
                && request.minimumNights() < 1) {

            throw new IllegalArgumentException(
                    "Minimum nights must be at least 1");
        }
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }
}