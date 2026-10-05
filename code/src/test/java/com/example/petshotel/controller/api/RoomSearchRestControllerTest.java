package com.example.petshotel.controller.api;

import java.util.List;

import com.example.petshotel.dto.request.RoomSearchRequest;
import com.example.petshotel.dto.response.PageResponse;
import com.example.petshotel.exception.GlobalExceptionHandler;
import com.example.petshotel.service.RoomSearchService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RoomSearchRestControllerTest {

    private RoomSearchService roomSearchService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        roomSearchService = mock(RoomSearchService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new RoomSearchRestController(roomSearchService))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void searchShouldPassPageSizeAndSortToService() throws Exception {
        when(roomSearchService.searchRooms(any(), any()))
                .thenReturn(new PageResponse<>(List.of(), 1, 5, 0, 0, false, true, "pricePerPetPerNight: DESC"));

        mockMvc.perform(get("/api/rooms/search")
                        .param("keyword", "deluxe")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "pricePerPetPerNight,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5));

        ArgumentCaptor<RoomSearchRequest> request = ArgumentCaptor.forClass(RoomSearchRequest.class);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(roomSearchService).searchRooms(request.capture(), pageable.capture());

        assertEquals("deluxe", request.getValue().keyword());
        assertEquals(1, pageable.getValue().getPageNumber());
        assertEquals(5, pageable.getValue().getPageSize());
        assertEquals(Sort.Direction.DESC,
                pageable.getValue().getSort().getOrderFor("pricePerPetPerNight").getDirection());
    }

    @Test
    void searchShouldReturn400WhenServiceRejectsSort() throws Exception {
        when(roomSearchService.searchRooms(any(), any()))
                .thenThrow(new IllegalArgumentException("ไม่สามารถเรียงตาม password ได้"));

        mockMvc.perform(get("/api/rooms/search").param("sort", "password,asc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchShouldReturn400WhenMinCapacityIsZero() throws Exception {
        mockMvc.perform(get("/api/rooms/search").param("minCapacity", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(roomSearchService);
    }
}