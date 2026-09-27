package com.example.petshotel.controller.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.petshotel.domain.entity.Promotion;
import com.example.petshotel.domain.enums.PromotionType;
import com.example.petshotel.dto.request.CreatePromotionRequest;
import com.example.petshotel.exception.GlobalExceptionHandler;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.mapper.PromotionMapper;
import com.example.petshotel.service.PromotionService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PromotionRestControllerTest {

    private PromotionService promotionService;
    private MockMvc mockMvc;
    private Promotion promotion;
    private CreatePromotionRequest validRequest;

    private static final String VALID_JSON = """
            {
                "name": "Save 10%",
                "type": "PERCENTAGE",
                "discountValue": 10,
                "startDate": "2026-10-01",
                "endDate": "2026-10-31",
                "active": true
            }
            """;

    @BeforeEach
    void setUp() {
        promotionService = mock(PromotionService.class);

        PromotionRestController controller =
                new PromotionRestController(
                        promotionService,
                        new PromotionMapper());

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        promotion = new Promotion();
        promotion.setId(1L);
        promotion.setName("Save 10%");
        promotion.setType(PromotionType.PERCENTAGE);
        promotion.setValue(new BigDecimal("10"));
        promotion.setStartDate(LocalDate.of(2026, 10, 1));
        promotion.setEndDate(LocalDate.of(2026, 10, 31));
        promotion.setActive(true);

        validRequest = new CreatePromotionRequest(
                "Save 10%",
                PromotionType.PERCENTAGE,
                new BigDecimal("10"),
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                true);
    }

    @Test
    void getAllPromotionsShouldReturnResponses() throws Exception {
        when(promotionService.getAllPromotions())
                .thenReturn(List.of(promotion));

        mockMvc.perform(get("/api/promotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Save 10%"))
                .andExpect(jsonPath("$[0].type").value("PERCENTAGE"))
                .andExpect(jsonPath("$[0].value").value(10));

        verify(promotionService).getAllPromotions();
    }

    @Test
    void getActivePromotionsShouldUseActiveServiceMethod()
            throws Exception {

        when(promotionService.getActivePromotions())
                .thenReturn(List.of(promotion));

        mockMvc.perform(get("/api/promotions/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(promotionService).getActivePromotions();
    }

    @Test
    void getPromotionByIdShouldReturnResponse() throws Exception {
        when(promotionService.getPromotionById(1L))
                .thenReturn(promotion);

        mockMvc.perform(get("/api/promotions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Save 10%"))
                .andExpect(jsonPath("$.startDate").value("2026-10-01"))
                .andExpect(jsonPath("$.endDate").value("2026-10-31"));

        verify(promotionService).getPromotionById(1L);
    }

    @Test
    void createPromotionShouldReturnCreated() throws Exception {
        when(promotionService.createPromotion(validRequest))
                .thenReturn(promotion);

        mockMvc.perform(post("/api/promotions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Save 10%"))
                .andExpect(jsonPath("$.value").value(10));

        verify(promotionService).createPromotion(validRequest);
    }

    @Test
    void updatePromotionShouldReturnUpdatedResponse() throws Exception {
        String updateJson = """
                {
                    "name": "Save 20%",
                    "type": "PERCENTAGE",
                    "discountValue": 20,
                    "startDate": "2026-10-01",
                    "endDate": "2026-10-31",
                    "active": true
                }
                """;

        CreatePromotionRequest updateRequest =
                new CreatePromotionRequest(
                        "Save 20%",
                        PromotionType.PERCENTAGE,
                        new BigDecimal("20"),
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31),
                        true);

        promotion.setName("Save 20%");
        promotion.setValue(new BigDecimal("20"));

        when(promotionService.updatePromotion(1L, updateRequest))
                .thenReturn(promotion);

        mockMvc.perform(put("/api/promotions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Save 20%"))
                .andExpect(jsonPath("$.value").value(20));

        verify(promotionService).updatePromotion(1L, updateRequest);
    }

    @Test
    void deactivatePromotionShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/promotions/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(promotionService).deactivatePromotion(1L);
    }

    @Test
    void getMissingPromotionShouldReturnNotFound() throws Exception {
        when(promotionService.getPromotionById(99L))
                .thenThrow(new ResourceNotFoundException("Promotion", 99L));

        mockMvc.perform(get("/api/promotions/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/promotions/99"));
    }

    @Test
    void createPromotionWithBlankNameShouldReturnBadRequest()
            throws Exception {

        String invalidJson = """
                {
                    "name": "",
                    "type": "PERCENTAGE",
                    "discountValue": 10,
                    "startDate": "2026-10-01",
                    "endDate": "2026-10-31",
                    "active": true
                }
                """;

        mockMvc.perform(post("/api/promotions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.name").exists());

        verifyNoInteractions(promotionService);
    }

    @Test
    void createPromotionShouldReturnBadRequestWhenServiceRejectsValue()
            throws Exception {

        String invalidJson = """
                {
                    "name": "Invalid discount",
                    "type": "PERCENTAGE",
                    "discountValue": 150,
                    "startDate": "2026-10-01",
                    "endDate": "2026-10-31",
                    "active": true
                }
                """;

        CreatePromotionRequest invalidRequest =
                new CreatePromotionRequest(
                        "Invalid discount",
                        PromotionType.PERCENTAGE,
                        new BigDecimal("150"),
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31),
                        true);

        when(promotionService.createPromotion(invalidRequest))
                .thenThrow(new IllegalArgumentException(
                        "Percentage discount must not exceed 100"));

        mockMvc.perform(post("/api/promotions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        "Percentage discount must not exceed 100"));

        verify(promotionService).createPromotion(invalidRequest);
    }
}