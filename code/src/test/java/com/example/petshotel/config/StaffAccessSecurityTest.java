package com.example.petshotel.config;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.petshotel.controller.web.AdminCareReportPageController;
import com.example.petshotel.controller.web.RoomController;
import com.example.petshotel.domain.entity.User;
import com.example.petshotel.domain.enums.UserRole;
import com.example.petshotel.service.CurrentUserService;
import com.example.petshotel.service.DailyCareReportService;
import com.example.petshotel.service.RoomService;

@WebMvcTest({AdminCareReportPageController.class, RoomController.class})
@Import(SecurityConfig.class)
class StaffAccessSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DailyCareReportService reportService;

    @MockitoBean
    private CurrentUserService currentUserService;

    @MockitoBean
    private RoomService roomService;

    @Test
    @WithMockUser(username = "staff@example.com", roles = "STAFF")
    void staffCanOpenCareReportPage() throws Exception {
        User staff = new User();
        staff.setId(5L);
        staff.setEmail("staff@example.com");
        staff.setRole(UserRole.STAFF);
        staff.setActive(true);
        when(currentUserService.getByEmail("staff@example.com")).thenReturn(staff);

        mockMvc.perform(get("/admin/reports"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCannotOpenAdminRoomPage() throws Exception {
        mockMvc.perform(get("/admin/rooms"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCannotOpenCareReportPage() throws Exception {
        mockMvc.perform(get("/admin/reports"))
                .andExpect(status().isForbidden());
    }
}