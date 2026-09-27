package com.example.petshotel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.petshotel.domain.entity.ExtraService;
import com.example.petshotel.repository.ExtraServiceRepository;
import com.example.petshotel.service.impl.ExtraServiceServiceImpl;
import com.example.petshotel.exception.ResourceNotFoundException;

public class ExtraServiceServiceTest {
    private ExtraServiceRepository extraServiceRepository;
    private ExtraServiceServiceImpl extraServiceService;

    @BeforeEach 
    void setUp(){
        extraServiceRepository = mock(ExtraServiceRepository.class);
        extraServiceService = new ExtraServiceServiceImpl(extraServiceRepository);
    }

    private ExtraService serviceData(String name, String price, Boolean active){
        ExtraService extraService = new ExtraService();
        extraService.setName(name);
        extraService.setPrice(new BigDecimal(price));
        extraService.setActive(active);
        return extraService;
    }

    @Test 
    void createsServiceWithActiveTrueByDefault(){
        ExtraService input = serviceData("  Bath  ", "200.00", null);

        when(extraServiceRepository.save(any(ExtraService.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        ExtraService result = extraServiceService.createExtraService(input);

        assertEquals("Bath", result.getName());
        assertEquals(new BigDecimal("200.00"), result.getPrice());
        assertTrue(result.getActive());
        verify(extraServiceRepository).save(result);
    }

    @Test
    void preservesExplicitInactiveValueWhenCreating(){
        ExtraService input = serviceData("Bath", "200.00", false);

        when(extraServiceRepository.save(any(ExtraService.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        ExtraService result = extraServiceService.createExtraService(input);

        assertFalse(result.getActive());
        verify(extraServiceRepository).save(result);
    }

    @Test 
    void updatesExistingServiceAndItsActiveValue(){
        ExtraService existing = serviceData("Old name", "100.00", true);
        existing.setId(1L);
        existing.setDescription("Old description");

        ExtraService details = serviceData("  Grooming  ", "300.00", false);
        details.setDescription("Bath and grooming");

        when(extraServiceRepository.findById(1L))
            .thenReturn(Optional.of(existing));
        when(extraServiceRepository.save(any(ExtraService.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        ExtraService result = extraServiceService.updateExtraService(1L, details);
        
        assertSame(existing, result);
        assertEquals(Long.valueOf(1L), result.getId());
        assertEquals("Grooming", result.getName());
        assertEquals("Bath and grooming", result.getDescription());
        assertEquals(new BigDecimal("300.00"), result.getPrice());
        assertFalse(result.getActive());
        verify(extraServiceRepository).save(existing);

    }

    @Test 
    void preservesActiveValueWhenUpdateDoesNotSpecifyIt(){
        ExtraService existing = serviceData("Bath", "200.00", false);
        existing.setId(1L);

        ExtraService details = serviceData("Bath", "250.00", null);

        when(extraServiceRepository.findById(1L))
            .thenReturn(Optional.of(existing));
        when(extraServiceRepository.save(any(ExtraService.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        ExtraService result = extraServiceService.updateExtraService(1L, details);
        
        assertFalse(result.getActive());
        assertEquals(new BigDecimal("250.00"), result.getPrice());
        verify(extraServiceRepository).save(existing);

    }

    @Test 
    void deactivatesServiceWithoutDeletingIt(){
        ExtraService existing = serviceData("Bath", "200.00", true);
        existing.setId(1L);

        when(extraServiceRepository.findById(1L))
            .thenReturn(Optional.of(existing));
        
        extraServiceService.deleteExtraService(1L);

        assertFalse(existing.getActive());
        verify(extraServiceRepository).save(existing);
        verify(extraServiceRepository, never()).delete(any(ExtraService.class));
        verify(extraServiceRepository, never()).deleteById(any(Long.class));
    }

    @Test 
    void rejectsBlankNameWhenCreating(){
        ExtraService input = serviceData("   ", "200.00", true);

        assertThrows(IllegalArgumentException.class, 
            () -> extraServiceService.createExtraService(input));

        verify(extraServiceRepository, never()).save(any(ExtraService.class));
    }

    @Test 
    void rejectsNegativePriceWhenCreating(){
        ExtraService input = serviceData("Bath", "-1.00", true);

        assertThrows(IllegalArgumentException.class,
            () -> extraServiceService.createExtraService(input));

        verify(extraServiceRepository, never()).save(any(ExtraService.class));
    }

    @Test 
    void rejectsNegativePriceWhenUpdating(){
        ExtraService details = serviceData("Bath", "-1.00", true);

        assertThrows(IllegalArgumentException.class, 
            () -> extraServiceService.updateExtraService(1L, details));
        verify(extraServiceRepository, never()).save(any(ExtraService.class));
    }

    @Test 
    void rejectsUpdatingMissingService(){
        ExtraService details = serviceData("Bath", "200.00", true);

        when(extraServiceRepository.findById(99L))
            .thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> extraServiceService.updateExtraService(99L, details));

        assertEquals("ExtraService not found: 99", exception.getMessage());
        verify(extraServiceRepository, never()).save(any(ExtraService.class));
    }

}

