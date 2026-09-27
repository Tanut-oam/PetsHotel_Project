package com.example.petshotel.controller.api;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.petshotel.domain.entity.ExtraService;
import com.example.petshotel.exception.GlobalExceptionHandler;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.mapper.ExtraServiceMapper;
import com.example.petshotel.service.ExtraServiceService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ExtraServiceRestControllerTest {

    private ExtraServiceService extraServiceService;
    private MockMvc mockMvc;
    private ExtraService extraService;

    private static final String VALID_JSON = """
            {
                "name": "Bath",
                "description": "Bath and dry",
                "price": 200.00,
                "active": true
            }
            """;

    @BeforeEach
    void setUp() {
        extraServiceService = mock(ExtraServiceService.class);

        ExtraServiceRestController controller =
                new ExtraServiceRestController(
                        extraServiceService,
                        new ExtraServiceMapper());

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        extraService = new ExtraService();
        extraService.setId(1L);
        extraService.setName("Bath");
        extraService.setDescription("Bath and dry");
        extraService.setPrice(new BigDecimal("200.00"));
        extraService.setActive(true);
    }

    @Test
    void getAllExtraServicesShouldReturnResponses() throws Exception {
        when(extraServiceService.getAllExtraServices())
                .thenReturn(List.of(extraService));

        mockMvc.perform(get("/api/extra-services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Bath"))
                .andExpect(jsonPath("$[0].price").value(200));

        verify(extraServiceService).getAllExtraServices();
    }

    @Test
    void getExtraServiceByIdShouldReturnResponse() throws Exception {
        when(extraServiceService.getExtraServiceById(1L))
                .thenReturn(extraService);

        mockMvc.perform(get("/api/extra-services/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Bath"))
                .andExpect(jsonPath("$.description").value("Bath and dry"))
                .andExpect(jsonPath("$.price").value(200))
                .andExpect(jsonPath("$.active").value(true));

        verify(extraServiceService).getExtraServiceById(1L);
    }

    @Test
    void createExtraServiceShouldMapRequestAndReturnCreated()
            throws Exception {

        when(extraServiceService.createExtraService(any(ExtraService.class)))
                .thenReturn(extraService);

        mockMvc.perform(post("/api/extra-services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Bath"))
                .andExpect(jsonPath("$.price").value(200));

        ArgumentCaptor<ExtraService> captor =
                ArgumentCaptor.forClass(ExtraService.class);

        verify(extraServiceService).createExtraService(captor.capture());

        ExtraService submitted = captor.getValue();

        assertNull(submitted.getId());
        assertEquals("Bath", submitted.getName());
        assertEquals("Bath and dry", submitted.getDescription());
        assertEquals(
                0,
                new BigDecimal("200.00").compareTo(submitted.getPrice()));
        assertEquals(Boolean.TRUE, submitted.getActive());
    }

    @Test
    void updateExtraServiceShouldMapRequestAndReturnUpdatedResponse()
            throws Exception {

        String updateJson = """
                {
                    "name": "Bath Premium",
                    "description": "Bath and grooming",
                    "price": 350.00,
                    "active": false
                }
                """;

        extraService.setName("Bath Premium");
        extraService.setDescription("Bath and grooming");
        extraService.setPrice(new BigDecimal("350.00"));
        extraService.setActive(false);

        when(extraServiceService.updateExtraService(
                eq(1L), any(ExtraService.class)))
                .thenReturn(extraService);

        mockMvc.perform(put("/api/extra-services/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Bath Premium"))
                .andExpect(jsonPath("$.price").value(350))
                .andExpect(jsonPath("$.active").value(false));

        ArgumentCaptor<ExtraService> captor =
                ArgumentCaptor.forClass(ExtraService.class);

        verify(extraServiceService)
                .updateExtraService(eq(1L), captor.capture());

        ExtraService submitted = captor.getValue();

        assertEquals("Bath Premium", submitted.getName());
        assertEquals("Bath and grooming", submitted.getDescription());
        assertEquals(
                0,
                new BigDecimal("350.00").compareTo(submitted.getPrice()));
        assertEquals(Boolean.FALSE, submitted.getActive());
    }

    @Test
    void deleteExtraServiceShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/extra-services/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(extraServiceService).deleteExtraService(1L);
    }

    @Test
    void getMissingExtraServiceShouldReturnNotFound() throws Exception {
        when(extraServiceService.getExtraServiceById(99L))
                .thenThrow(
                        new ResourceNotFoundException("ExtraService", 99L));

        mockMvc.perform(get("/api/extra-services/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/extra-services/99"));
    }

    @Test
    void createExtraServiceWithBlankNameShouldReturnBadRequest()
            throws Exception {

        String invalidJson = """
                {
                    "name": "",
                    "description": "Bath and dry",
                    "price": 200.00,
                    "active": true
                }
                """;

        mockMvc.perform(post("/api/extra-services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());

        verifyNoInteractions(extraServiceService);
    }

    @Test
    void createExtraServiceWithNegativePriceShouldReturnBadRequest()
            throws Exception {

        String invalidJson = """
                {
                    "name": "Bath",
                    "price": -1.00,
                    "active": true
                }
                """;

        mockMvc.perform(post("/api/extra-services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.price").exists());

        verifyNoInteractions(extraServiceService);
    }

    @Test
    void updateExtraServiceWithTooManyDecimalPlacesShouldReturnBadRequest()
            throws Exception {

        String invalidJson = """
                {
                    "name": "Bath",
                    "price": 200.123,
                    "active": true
                }
                """;

        mockMvc.perform(put("/api/extra-services/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.price").exists());

        verifyNoInteractions(extraServiceService);
    }
}