package com.example.petshotel.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petshotel.domain.entity.Promotion;
import com.example.petshotel.repository.PromotionRepository;
import com.example.petshotel.service.PromotionService;

@Service 
@Transactional
public class PromotionServiceImpl implements PromotionService{
    
    private final PromotionRepository promotionRepository;

    public PromotionServiceImpl(PromotionRepository promotionRepository){
        this.promotionRepository = promotionRepository;
    }

    @Override 
    @Transactional(readOnly = true)
    public List<Promotion> getAllPromotions(){
        return promotionRepository.findAll();
    }

    @Override 
    @Transactional(readOnly = true)
    public List<Promotion> getActivePromotions(){
        return promotionRepository.findByActiveTrue();
    }

    @Override 
    @Transactional(readOnly = true)
    public Optional<Promotion> getPromotionById(Long id){
        return promotionRepository.findById(id);
    }

    @Override 
    public Promotion createPromotion(Promotion promotion){
        return promotionRepository.save(promotion);
    }

    @Override 
    public Promotion updatePromotion(Long id, Promotion promotionDetails){
        return promotionRepository.findById(id)
                .map(existingPromotion->{
                    existingPromotion.setName(promotionDetails.getName());
                    existingPromotion.setDescription(promotionDetails.getDescription());
                    existingPromotion.setType(promotionDetails.getType());
                    existingPromotion.setValue(promotionDetails.getValue());
                    existingPromotion.setMinimumNights(promotionDetails.getMinimumNights());
                    existingPromotion.setStartDate(promotionDetails.getStartDate());
                    existingPromotion.setEndDate(promotionDetails.getEndDate());
                    existingPromotion.setActive(promotionDetails.getActive());
                    return promotionRepository.save(existingPromotion);
                })
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));
    }

    @Override 
    public void deletePromotion(Long id){
        promotionRepository.deleteById(id);
    }


}
