package com.example.petshotel.controller.web;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.petshotel.config.SecurityConfig;
import com.example.petshotel.domain.entity.ExtraService;
import com.example.petshotel.domain.enums.RoomStatus;
import com.example.petshotel.dto.response.RoomResponse;
import com.example.petshotel.mapper.ExtraServiceMapper;
import com.example.petshotel.service.ExtraServiceService;
import com.example.petshotel.service.RoomService;

@WebMvcTest(HomeController.class)
@Import({SecurityConfig.class, ExtraServiceMapper.class})
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    @MockitoBean
    private ExtraServiceService extraServiceService;

    private RoomResponse room(Long id) {
        return new RoomResponse(id, "10" + id, "Room " + id, null, 2,
                new BigDecimal("500.00"), RoomStatus.ACTIVE, null);
    }

    private ExtraService service(Long id, String name) {
        ExtraService s = new ExtraService();
        s.setId(id);
        s.setName(name);
        s.setPrice(new BigDecimal("300.00"));
        s.setActive(true);
        return s;
    }

    @Test
    void home_showsAtMostThreeActiveRooms() throws Exception {
        when(roomService.getActiveRooms())
                .thenReturn(List.of(room(1L), room(2L), room(3L), room(4L)));

        mockMvc.perform(get("/").with(user("customer@example.com").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("rooms", List.of(room(1L), room(2L), room(3L))));
    }

    @Test
    void home_showsAtMostFourActiveServices() throws Exception {
        when(roomService.getActiveRooms()).thenReturn(List.of());
        when(extraServiceService.getActiveExtraServices()).thenReturn(List.of(
                service(1L, "Bath"), service(2L, "Walk"), service(3L, "Nail"),
                service(4L, "Food"), service(5L, "Health")));

        mockMvc.perform(get("/").with(user("customer@example.com").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("services", hasSize(4)));
    }

    @Test
    void home_redirectsAnonymousUserToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}