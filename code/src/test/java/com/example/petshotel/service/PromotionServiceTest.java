package com.example.petshotel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.petshotel.domain.entity.Promotion;
import com.example.petshotel.domain.enums.PromotionType;
import com.example.petshotel.dto.request.CreatePromotionRequest;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.repository.PromotionRepository;
import com.example.petshotel.service.impl.PromotionServiceImpl;

public class PromotionServiceTest {

    private PromotionRepository promotionRepository;
    private PromotionServiceImpl promotionService;

    @BeforeEach
    void setUp() {
        promotionRepository = mock(PromotionRepository.class);
        promotionService = new PromotionServiceImpl(promotionRepository);
    }

    private CreatePromotionRequest request(
            PromotionType type,
            String value) {

        return request(type, value, "  Stay at least 3 nights  ", 3);
    }

    private CreatePromotionRequest request(
            PromotionType type,
            String value,
            String description,
            Integer minimumNights) {

        return new CreatePromotionRequest(
                "  Summer  ",
                type,
                new BigDecimal(value),
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30),
                true,
                description,
                minimumNights);
    }

    @Test
    void createsPromotionAndAllowsExactlyOneHundredPercent() {
        CreatePromotionRequest request =
                request(PromotionType.PERCENTAGE, "100");

        when(promotionRepository.save(any(Promotion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Promotion result = promotionService.createPromotion(request);

        assertEquals("Summer", result.getName());
        assertEquals(PromotionType.PERCENTAGE, result.getType());
        assertEquals(new BigDecimal("100"), result.getValue());
        assertEquals(request.startDate(), result.getStartDate());
        assertEquals(request.endDate(), result.getEndDate());
        assertTrue(result.getActive());
        assertEquals("Stay at least 3 nights", result.getDescription());
        assertEquals(Integer.valueOf(3), result.getMinimumNights());

        verify(promotionRepository).save(result);
    }

    @Test
    void updatesExistingPromotionWithFixedAmount() {
        Promotion existing = new Promotion();
        existing.setId(1L);
        existing.setName("Old promotion");
        existing.setType(PromotionType.PERCENTAGE);
        existing.setValue(new BigDecimal("10"));
        existing.setStartDate(LocalDate.of(2026, 1, 1));
        existing.setEndDate(LocalDate.of(2026, 1, 31));
        existing.setActive(false);
        existing.setDescription("Old description");
        existing.setMinimumNights(1);

        CreatePromotionRequest request =
                request(PromotionType.FIXED_AMOUNT, "500.00");

        when(promotionRepository.findById(1L))
                .thenReturn(Optional.of(existing));
        when(promotionRepository.save(any(Promotion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Promotion result =
                promotionService.updatePromotion(1L, request);

        assertSame(existing, result);
        assertEquals(Long.valueOf(1L), result.getId());
        assertEquals("Summer", result.getName());
        assertEquals(PromotionType.FIXED_AMOUNT, result.getType());
        assertEquals(new BigDecimal("500.00"), result.getValue());
        assertEquals(request.startDate(), result.getStartDate());
        assertEquals(request.endDate(), result.getEndDate());
        assertTrue(result.getActive());
        assertEquals("Stay at least 3 nights", result.getDescription());
        assertEquals(Integer.valueOf(3), result.getMinimumNights());

        verify(promotionRepository).save(existing);
    }

    @Test
    void createsPromotionWithoutOptionalFields() {
        CreatePromotionRequest request =
                request(PromotionType.PERCENTAGE, "10", null, null);

        when(promotionRepository.save(any(Promotion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Promotion result = promotionService.createPromotion(request);

        assertNull(result.getDescription());
        assertNull(result.getMinimumNights());

        verify(promotionRepository).save(result);
    }

    @Test
    void clearsDescriptionAndMinimumNightsWhenEditFieldsAreBlank() {
        Promotion existing = new Promotion();
        existing.setId(1L);
        existing.setDescription("Old description");
        existing.setMinimumNights(3);

        CreatePromotionRequest request =
                request(PromotionType.PERCENTAGE, "10", "   ", null);

        when(promotionRepository.findById(1L))
                .thenReturn(Optional.of(existing));
        when(promotionRepository.save(any(Promotion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Promotion result =
                promotionService.updatePromotion(1L, request);

        assertSame(existing, result);
        assertNull(result.getDescription());
        assertNull(result.getMinimumNights());

        verify(promotionRepository).save(existing);
    }

    @Test
    void deactivatesPromotionWithoutDeletingIt() {
        Promotion existing = new Promotion();
        existing.setId(1L);
        existing.setActive(true);

        when(promotionRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        promotionService.deactivatePromotion(1L);

        assertFalse(existing.getActive());

        verify(promotionRepository).save(existing);
        verify(promotionRepository, never()).delete(any(Promotion.class));
        verify(promotionRepository, never()).deleteById(any(Long.class));
    }

    @Test
    void rejectsPercentageAboveOneHundredWhenCreating() {
        CreatePromotionRequest request =
                request(PromotionType.PERCENTAGE, "150");

        assertThrows(
                IllegalArgumentException.class,
                () -> promotionService.createPromotion(request));

        verify(promotionRepository, never()).save(any(Promotion.class));
    }

    @Test
    void rejectsPercentageAboveOneHundredWhenUpdating() {
        CreatePromotionRequest request =
                request(PromotionType.PERCENTAGE, "150");

        assertThrows(
                IllegalArgumentException.class,
                () -> promotionService.updatePromotion(1L, request));

        verify(promotionRepository, never()).save(any(Promotion.class));
    }

    @Test
    void rejectsZeroDiscount() {
        CreatePromotionRequest request =
                request(PromotionType.FIXED_AMOUNT, "0");

        assertThrows(
                IllegalArgumentException.class,
                () -> promotionService.createPromotion(request));

        verify(promotionRepository, never()).save(any(Promotion.class));
    }

    @Test
    void rejectsEndDateBeforeStartDate() {
        CreatePromotionRequest request = new CreatePromotionRequest(
                "Summer",
                PromotionType.PERCENTAGE,
                new BigDecimal("10"),
                LocalDate.of(2026, 4, 30),
                LocalDate.of(2026, 4, 1),
                true,
                null,
                null);

        assertThrows(
                IllegalArgumentException.class,
                () -> promotionService.createPromotion(request));

        verify(promotionRepository, never()).save(any(Promotion.class));
    }

    @Test
    void rejectsZeroMinimumNightsWhenCreating() {
        CreatePromotionRequest request =
                request(PromotionType.PERCENTAGE, "10", null, 0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> promotionService.createPromotion(request));

        assertEquals(
                "Minimum nights must be at least 1",
                exception.getMessage());

        verify(promotionRepository, never()).save(any(Promotion.class));
    }

    @Test
    void rejectsNegativeMinimumNightsWhenUpdating() {
        CreatePromotionRequest request =
                request(PromotionType.PERCENTAGE, "10", null, -1);

        assertThrows(
                IllegalArgumentException.class,
                () -> promotionService.updatePromotion(1L, request));

        verify(promotionRepository, never()).findById(any(Long.class));
        verify(promotionRepository, never()).save(any(Promotion.class));
    }

    @Test
    void rejectsUpdatingMissingPromotion() {
        CreatePromotionRequest request =
                request(PromotionType.PERCENTAGE, "10");

        when(promotionRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> promotionService.updatePromotion(99L, request));

        assertEquals("Promotion not found: 99", exception.getMessage());

        verify(promotionRepository, never()).save(any(Promotion.class));
    }
}