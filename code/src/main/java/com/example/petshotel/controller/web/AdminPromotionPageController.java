package com.example.petshotel.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.petshotel.domain.entity.Promotion;
import com.example.petshotel.domain.enums.PromotionType;
import com.example.petshotel.dto.request.CreatePromotionRequest;
import com.example.petshotel.dto.response.PromotionResponse;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.mapper.PromotionMapper;
import com.example.petshotel.service.PromotionService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/promotions")
public class AdminPromotionPageController {

    private final PromotionService promotionService;
    private final PromotionMapper promotionMapper;

    public AdminPromotionPageController(
            PromotionService promotionService,
            PromotionMapper promotionMapper) {

        this.promotionService = promotionService;
        this.promotionMapper = promotionMapper;
    }

    @GetMapping
    public String showPromotions(
            @RequestParam(name = "editId", required = false) Long editId,
            Model model) {

        List<PromotionResponse> responses = new ArrayList<>();

        for (Promotion promotion : promotionService.getAllPromotions()) {
            responses.add(promotionMapper.toResponse(promotion));
        }

        model.addAttribute("promotions", responses);
        model.addAttribute("editingId", editId);

        if (!model.containsAttribute("promotionForm")) {
            CreatePromotionRequest form;

            if (editId == null) {
                form = new CreatePromotionRequest(
                        "",
                        PromotionType.PERCENTAGE,
                        null,
                        null,
                        null,
                        true,
                        null,
                        null);
            } else {
                Promotion promotion =
                        promotionService.getPromotionById(editId);

                form = new CreatePromotionRequest(
                        promotion.getName(),
                        promotion.getType(),
                        promotion.getValue(),
                        promotion.getStartDate(),
                        promotion.getEndDate(),
                        Boolean.TRUE.equals(promotion.getActive()),
                        promotion.getDescription(),
                        promotion.getMinimumNights());
            }

            model.addAttribute("promotionForm", form);
        }

        return "admin/promotions";
    }

    @PostMapping
    public String savePromotion(
            @RequestParam(name = "id", required = false) Long id,
            @Valid @ModelAttribute("promotionForm")
            CreatePromotionRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return showPromotions(id, model);
        }

        try {
            if (id == null) {
                promotionService.createPromotion(request);
            } else {
                promotionService.updatePromotion(id, request);
            }
        } catch (IllegalArgumentException exception) {
            bindingResult.reject(
                    "promotion.invalid",
                    exception.getMessage());

            return showPromotions(id, model);
        }

        redirectAttributes.addFlashAttribute(
                "message",
                id == null
                        ? "เพิ่มโปรโมชันสำเร็จ"
                        : "แก้ไขโปรโมชันสำเร็จ");

        return "redirect:/admin/promotions";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivatePromotion(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        promotionService.deactivatePromotion(id);

        redirectAttributes.addFlashAttribute(
                "message",
                "ปิดใช้โปรโมชันสำเร็จ");

        return "redirect:/admin/promotions";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(
            ResourceNotFoundException exception,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute(
                "error",
                exception.getMessage());

        return "redirect:/admin/promotions";
    }
}