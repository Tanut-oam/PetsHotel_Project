package com.example.petshotel.controller.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
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
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.petshotel.domain.entity.ExtraService;
import com.example.petshotel.dto.request.ExtraServiceRequest;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.mapper.ExtraServiceMapper;
import com.example.petshotel.service.ExtraServiceService;

class AdminExtraServicePageControllerTest {

    private ExtraServiceService extraServiceService;
    private ExtraServiceMapper extraServiceMapper;
    private MockMvc mockMvc;
    private ExtraService extraService;

    @BeforeEach
    void setUp() {
        extraServiceService = mock(ExtraServiceService.class);
        extraServiceMapper = new ExtraServiceMapper();

        AdminExtraServicePageController controller =
                new AdminExtraServicePageController(
                        extraServiceService,
                        extraServiceMapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        extraService = new ExtraService();
        extraService.setId(1L);
        extraService.setName("Bath");
        extraService.setDescription("Bath and dry");
        extraService.setPrice(new BigDecimal("200.00"));
        extraService.setActive(true);

        when(extraServiceService.getAllExtraServices())
                .thenReturn(List.of(extraService));
    }

    private MockHttpServletRequestBuilder form(
            String name,
            String price,
            boolean active) {

        MockHttpServletRequestBuilder request = post("/admin/services")
                .param("name", name)
                .param("description", "Bath and dry")
                .param("price", price)
                .param("_active", "on");

        if (active) {
            request.param("active", "true");
        }

        return request;
    }

    private ExtraServiceRequest expectedRequest(
            String name,
            String price,
            boolean active) {

        return new ExtraServiceRequest(
                name,
                "Bath and dry",
                new BigDecimal(price),
                active);
    }

    @Test
    void showsServicesWithEmptyCreateForm() throws Exception {
        ExtraServiceRequest emptyForm = new ExtraServiceRequest(
                "",
                "",
                null,
                true);

        mockMvc.perform(get("/admin/services"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/services"))
                .andExpect(model().attribute(
                        "extraServices",
                        List.of(extraServiceMapper.toResponse(extraService))))
                .andExpect(model().attribute("serviceForm", emptyForm));

        verify(extraServiceService).getAllExtraServices();
        verify(extraServiceService, never())
                .getExtraServiceById(anyLong());
    }

    @Test
    void loadsServiceIntoEditForm() throws Exception {
        when(extraServiceService.getExtraServiceById(1L))
                .thenReturn(extraService);

        mockMvc.perform(get("/admin/services")
                        .param("editId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/services"))
                .andExpect(model().attribute("editingId", 1L))
                .andExpect(model().attribute(
                        "serviceForm",
                        expectedRequest("Bath", "200.00", true)));

        verify(extraServiceService).getExtraServiceById(1L);
    }

    @Test
    void createsServiceFromForm() throws Exception {
        mockMvc.perform(form("Bath", "200.00", true))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/services"))
                .andExpect(flash().attribute(
                        "message",
                        "เพิ่มบริการเสริมสำเร็จ"));

        ArgumentCaptor<ExtraService> captor =
                ArgumentCaptor.forClass(ExtraService.class);

        verify(extraServiceService).createExtraService(captor.capture());

        ExtraService saved = captor.getValue();

        assertEquals("Bath", saved.getName());
        assertEquals("Bath and dry", saved.getDescription());
        assertEquals(new BigDecimal("200.00"), saved.getPrice());
        assertTrue(saved.getActive());

        verify(extraServiceService, never())
                .updateExtraService(anyLong(), any(ExtraService.class));
    }

    @Test
    void updatesServiceFromForm() throws Exception {
        mockMvc.perform(form("Bath Premium", "250.00", true)
                        .param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/services"))
                .andExpect(flash().attribute(
                        "message",
                        "แก้ไขบริการเสริมสำเร็จ"));

        ArgumentCaptor<ExtraService> captor =
                ArgumentCaptor.forClass(ExtraService.class);

        verify(extraServiceService)
                .updateExtraService(eq(1L), captor.capture());

        ExtraService updated = captor.getValue();

        assertEquals("Bath Premium", updated.getName());
        assertEquals("Bath and dry", updated.getDescription());
        assertEquals(new BigDecimal("250.00"), updated.getPrice());
        assertTrue(updated.getActive());

        verify(extraServiceService, never())
                .createExtraService(any(ExtraService.class));
    }

    @Test
    void uncheckedCheckboxMakesServiceInactive() throws Exception {
        mockMvc.perform(form("Bath", "200.00", false))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/services"));

        ArgumentCaptor<ExtraService> captor =
                ArgumentCaptor.forClass(ExtraService.class);

        verify(extraServiceService).createExtraService(captor.capture());

        assertFalse(captor.getValue().getActive());
    }

    @Test
    void rejectsBlankNameWithoutSaving() throws Exception {
        mockMvc.perform(form("", "200.00", true))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/services"))
                .andExpect(model().attributeHasFieldErrors(
                        "serviceForm",
                        "name"))
                .andExpect(model().attribute(
                        "serviceForm",
                        expectedRequest("", "200.00", true)));

        verify(extraServiceService, never())
                .createExtraService(any(ExtraService.class));
        verify(extraServiceService, never())
                .updateExtraService(anyLong(), any(ExtraService.class));
    }

    @Test
    void rejectsNegativePriceWithoutSaving() throws Exception {
        mockMvc.perform(form("Bath", "-10.00", true))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/services"))
                .andExpect(model().attributeHasFieldErrors(
                        "serviceForm",
                        "price"));

        verify(extraServiceService, never())
                .createExtraService(any(ExtraService.class));
        verify(extraServiceService, never())
                .updateExtraService(anyLong(), any(ExtraService.class));
    }

    @Test
    void keepsEditFormWhenValidationFails() throws Exception {
        mockMvc.perform(form("Bath Premium", "-10.00", false)
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/services"))
                .andExpect(model().attributeHasFieldErrors(
                        "serviceForm",
                        "price"))
                .andExpect(model().attribute("editingId", 1L))
                .andExpect(model().attribute(
                        "serviceForm",
                        expectedRequest("Bath Premium", "-10.00", false)));

        // ไม่โหลดข้อมูลเดิมมาทับค่าที่ผู้ใช้เพิ่งกรอก
        verify(extraServiceService, never())
                .getExtraServiceById(anyLong());
        verify(extraServiceService, never())
                .createExtraService(any(ExtraService.class));
        verify(extraServiceService, never())
                .updateExtraService(anyLong(), any(ExtraService.class));
    }

    @Test
    void deactivatesService() throws Exception {
        mockMvc.perform(post("/admin/services/1/deactivate"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/services"))
                .andExpect(flash().attribute(
                        "message",
                        "ปิดใช้บริการเสริมสำเร็จ"));

        verify(extraServiceService).deleteExtraService(1L);
    }

    @Test
    void redirectsWithErrorWhenServiceIsMissing() throws Exception {
        when(extraServiceService.getExtraServiceById(99L))
                .thenThrow(new ResourceNotFoundException(
                        "ExtraService",
                        99L));

        mockMvc.perform(get("/admin/services")
                        .param("editId", "99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/services"))
                .andExpect(flash().attribute(
                        "error",
                        "ExtraService not found: 99"));

        verify(extraServiceService).getExtraServiceById(99L);
    }
}