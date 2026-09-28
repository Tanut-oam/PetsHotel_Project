package com.example.petshotel.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.petshotel.dto.response.PromotionResponse;
import com.example.petshotel.mapper.PromotionMapper;
import com.example.petshotel.service.PromotionService;
import com.example.petshotel.domain.entity.Promotion;

@Controller 
public class AdminPromotionPageController {
    private final PromotionService promotionService;
    private final PromotionMapper promotionMapper;

    public AdminPromotionPageController(PromotionService promotionService, PromotionMapper promotionMapper){
        this.promotionService = promotionService;
        this.promotionMapper = promotionMapper;
    }

    @GetMapping("/admin/promotions")
    public String showPromotions(Model model){
        List<PromotionResponse> responses = new ArrayList<>();

        for(Promotion promotion: promotionService.getAllPromotions()){
            PromotionResponse response = promotionMapper.toResponse(promotion);
            responses.add(response);
        }

        model.addAttribute("promotions", responses);
        return "admin/promotions";
    }

}
