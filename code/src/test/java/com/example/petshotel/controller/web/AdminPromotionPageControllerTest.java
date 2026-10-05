package com.example.petshotel.controller.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.petshotel.domain.entity.Promotion;
import com.example.petshotel.domain.enums.PromotionType;
import com.example.petshotel.dto.request.CreatePromotionRequest;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.mapper.PromotionMapper;
import com.example.petshotel.service.PromotionService;

class AdminPromotionPageControllerTest {

    private PromotionService promotionService;
    private PromotionMapper promotionMapper;
    private MockMvc mockMvc;
    private Promotion promotion;

    @BeforeEach
    void setUp() {
        promotionService = mock(PromotionService.class);
        promotionMapper = new PromotionMapper();

        AdminPromotionPageController controller =
                new AdminPromotionPageController(
                        promotionService,
                        promotionMapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        promotion = new Promotion();
        promotion.setId(1L);
        promotion.setName("Save 10%");
        promotion.setType(PromotionType.PERCENTAGE);
        promotion.setValue(new BigDecimal("10.00"));
        promotion.setStartDate(LocalDate.of(2026, 10, 1));
        promotion.setEndDate(LocalDate.of(2026, 10, 31));
        promotion.setActive(true);

        when(promotionService.getAllPromotions())
                .thenReturn(List.of(promotion));
    }

    private MockHttpServletRequestBuilder form(
            String name,
            String discountValue,
            boolean active) {

        MockHttpServletRequestBuilder request = post("/admin/promotions")
                .param("name", name)
                .param("type", "PERCENTAGE")
                .param("discountValue", discountValue)
                .param("startDate", "2026-10-01")
                .param("endDate", "2026-10-31")
                .param("_active", "on");

        if (active) {
            request.param("active", "true");
        }

        return request;
    }

    private CreatePromotionRequest expectedRequest(
            String name,
            String discountValue,
            boolean active) {

        return new CreatePromotionRequest(
                name,
                PromotionType.PERCENTAGE,
                new BigDecimal(discountValue),
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                active);
    }

    @Test
    void showsPromotionsWithEmptyCreateForm() throws Exception {
        CreatePromotionRequest emptyForm = new CreatePromotionRequest(
                "",
                PromotionType.PERCENTAGE,
                null,
                null,
                null,
                true);

        mockMvc.perform(get("/admin/promotions"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/promotions"))
                .andExpect(model().attribute(
                        "promotions",
                        List.of(promotionMapper.toResponse(promotion))))
                .andExpect(model().attribute("promotionForm", emptyForm));

        verify(promotionService).getAllPromotions();
        verify(promotionService, never()).getPromotionById(anyLong());
    }

    @Test
    void loadsPromotionIntoEditForm() throws Exception {
        when(promotionService.getPromotionById(1L))
                .thenReturn(promotion);

        mockMvc.perform(get("/admin/promotions")
                        .param("editId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/promotions"))
                .andExpect(model().attribute("editingId", 1L))
                .andExpect(model().attribute(
                        "promotionForm",
                        expectedRequest("Save 10%", "10.00", true)));

        verify(promotionService).getPromotionById(1L);
    }

    @Test
    void createsPromotionFromForm() throws Exception {
        CreatePromotionRequest expected =
                expectedRequest("Save 10%", "10.00", true);

        mockMvc.perform(form("Save 10%", "10.00", true))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/promotions"))
                .andExpect(flash().attribute(
                        "message",
                        "เพิ่มโปรโมชันสำเร็จ"));

        verify(promotionService).createPromotion(expected);
        verify(promotionService, never())
                .updatePromotion(anyLong(), any(CreatePromotionRequest.class));
    }

    @Test
    void updatesPromotionFromForm() throws Exception {
        CreatePromotionRequest expected =
                expectedRequest("Save 20%", "20.00", true);

        mockMvc.perform(form("Save 20%", "20.00", true)
                        .param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/promotions"))
                .andExpect(flash().attribute(
                        "message",
                        "แก้ไขโปรโมชันสำเร็จ"));

        verify(promotionService).updatePromotion(1L, expected);
        verify(promotionService, never())
                .createPromotion(any(CreatePromotionRequest.class));
    }

    @Test
    void uncheckedCheckboxMakesPromotionInactive() throws Exception {
        CreatePromotionRequest expected =
                expectedRequest("Save 10%", "10.00", false);

        mockMvc.perform(form("Save 10%", "10.00", false))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/promotions"));

        verify(promotionService).createPromotion(expected);
    }

    @Test
    void rejectsBlankNameWithoutSaving() throws Exception {
        mockMvc.perform(form("", "10.00", true))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/promotions"))
                .andExpect(model().attributeHasFieldErrors(
                        "promotionForm",
                        "name"))
                .andExpect(model().attribute(
                        "promotionForm",
                        expectedRequest("", "10.00", true)));

        verify(promotionService, never())
                .createPromotion(any(CreatePromotionRequest.class));
        verify(promotionService, never())
                .updatePromotion(anyLong(), any(CreatePromotionRequest.class));
    }

    @Test
    void rejectsInvalidDateWithoutSaving() throws Exception {
        mockMvc.perform(post("/admin/promotions")
                        .param("name", "Save 10%")
                        .param("type", "PERCENTAGE")
                        .param("discountValue", "10.00")
                        .param("startDate", "not-a-date")
                        .param("endDate", "2026-10-31")
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/promotions"))
                .andExpect(model().attributeHasFieldErrors(
                        "promotionForm",
                        "startDate"));

        verify(promotionService, never())
                .createPromotion(any(CreatePromotionRequest.class));
        verify(promotionService, never())
                .updatePromotion(anyLong(), any(CreatePromotionRequest.class));
    }

    @Test
    void keepsEditFormWhenServiceRejectsDiscount() throws Exception {
        CreatePromotionRequest invalidRequest =
                expectedRequest("Save 150%", "150.00", true);

        when(promotionService.updatePromotion(1L, invalidRequest))
                .thenThrow(new IllegalArgumentException(
                        "Percentage discount must not exceed 100"));

        mockMvc.perform(form("Save 150%", "150.00", true)
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/promotions"))
                .andExpect(model().attributeHasErrors("promotionForm"))
                .andExpect(model().attribute("editingId", 1L))
                .andExpect(model().attribute(
                        "promotionForm",
                        invalidRequest));

        verify(promotionService).updatePromotion(1L, invalidRequest);

        // ไม่โหลดข้อมูลเดิมมาทับค่าที่ผู้ใช้เพิ่งกรอก
        verify(promotionService, never()).getPromotionById(anyLong());
        verify(promotionService, never())
                .createPromotion(any(CreatePromotionRequest.class));
    }

    @Test
    void deactivatesPromotion() throws Exception {
        mockMvc.perform(post("/admin/promotions/1/deactivate"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/promotions"))
                .andExpect(flash().attribute(
                        "message",
                        "ปิดใช้โปรโมชันสำเร็จ"));

        verify(promotionService).deactivatePromotion(1L);
    }

    @Test
    void redirectsWithErrorWhenPromotionIsMissing() throws Exception {
        when(promotionService.getPromotionById(99L))
                .thenThrow(new ResourceNotFoundException("Promotion", 99L));

        mockMvc.perform(get("/admin/promotions")
                        .param("editId", "99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/promotions"))
                .andExpect(flash().attribute(
                        "error",
                        "Promotion not found: 99"));

        verify(promotionService).getPromotionById(99L);
    }
}