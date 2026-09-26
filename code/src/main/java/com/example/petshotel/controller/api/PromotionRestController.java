package com.example.petshotel.controller.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.petshotel.dto.request.CreatePromotionRequest;
import com.example.petshotel.dto.response.PromotionResponse;
import com.example.petshotel.mapper.PromotionMapper;
import com.example.petshotel.service.PromotionService;

import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/promotions")
public class PromotionRestController {
    private final PromotionService promotionService;
    private final PromotionMapper promotionMapper;

    public PromotionRestController(
            PromotionService promotionService,
            PromotionMapper promotionMapper){
        this.promotionService = promotionService;
        this.promotionMapper = promotionMapper;
    }

    @GetMapping 
    public List<PromotionResponse> getAllPromotions(){
        return promotionService.getAllPromotions().stream()
                .map(promotionMapper::toResponse).toList();
    }

    @GetMapping("/active")
    public List<PromotionResponse> getActivePromotions(){
        return promotionService.getActivePromotions().stream()
                .map(promotionMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public PromotionResponse getPromotionById(@PathVariable Long id){
        return promotionMapper.toResponse(
            promotionService.getPromotionById(id));
    }

    @PostMapping 
    public ResponseEntity<PromotionResponse> createPromotion(@Valid @RequestBody CreatePromotionRequest request){
        PromotionResponse response = promotionMapper.toResponse(
            promotionService.createPromotion(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public PromotionResponse updatePromotion(@PathVariable Long id, @Valid @RequestBody CreatePromotionRequest request){
        return promotionMapper.toResponse(
            promotionService.updatePromotion(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivatePromotion(@PathVariable Long id){
        promotionService.deactivatePromotion(id);
        return ResponseEntity.noContent().build();
    }

}
