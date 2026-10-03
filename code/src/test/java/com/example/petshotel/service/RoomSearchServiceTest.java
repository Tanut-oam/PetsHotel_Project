package com.example.petshotel.service;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.domain.enums.RoomStatus;
import com.example.petshotel.dto.request.RoomSearchRequest;
import com.example.petshotel.dto.response.PageResponse;
import com.example.petshotel.dto.response.RoomResponse;
import com.example.petshotel.mapper.RoomMapper;
import com.example.petshotel.repository.RoomRepository;
import com.example.petshotel.service.impl.RoomSearchServiceImpl;

class RoomSearchServiceTest {

    private RoomRepository roomRepository;
    private RoomSearchService roomSearchService;

    @BeforeEach
    void setUp() {
        roomRepository = mock(RoomRepository.class);
        roomSearchService = new RoomSearchServiceImpl(roomRepository, new RoomMapper());
    }

    @Test
    void searchShouldApplyDefaultsAndReturnPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("roomNumber"));
        when(roomRepository.searchRooms(eq(RoomStatus.ACTIVE), eq("%%"), eq(1), any(BigDecimal.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(room("A101")), pageable, 11));

        PageResponse<RoomResponse> result =
                roomSearchService.searchRooms(new RoomSearchRequest(null, null, null), pageable);

        assertEquals(1, result.content().size());
        assertEquals("A101", result.content().get(0).roomNumber());
        assertEquals(0, result.page());
        assertEquals(11, result.totalElements());
        assertEquals(2, result.totalPages());
        assertTrue(result.first());
        assertFalse(result.last());
    }

    @Test
    void searchShouldLowercaseKeywordAndPassFilters() {
        Pageable pageable = PageRequest.of(1, 5, Sort.by(Sort.Direction.DESC, "pricePerPetPerNight"));
        when(roomRepository.searchRooms(any(), anyString(), anyInt(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        roomSearchService.searchRooms(new RoomSearchRequest("  DeLuxe ", 2, new BigDecimal("800")), pageable);

        verify(roomRepository).searchRooms(RoomStatus.ACTIVE, "%deluxe%", 2, new BigDecimal("800"), pageable);
    }

    @Test
    void searchShouldRejectUnknownSortField() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("password"));

        assertThrows(IllegalArgumentException.class,
                () -> roomSearchService.searchRooms(new RoomSearchRequest(null, null, null), pageable));
        verifyNoInteractions(roomRepository);
    }

    @Test
    void searchShouldRejectTooLargePageSize() {
        Pageable pageable = PageRequest.of(0, 51);

        assertThrows(IllegalArgumentException.class,
                () -> roomSearchService.searchRooms(new RoomSearchRequest(null, null, null), pageable));
        verifyNoInteractions(roomRepository);
    }

    private Room room(String roomNumber) {
        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber(roomNumber);
        room.setName("Deluxe");
        room.setCapacity(3);
        room.setPricePerPetPerNight(new BigDecimal("500.00"));
        room.setStatus(RoomStatus.ACTIVE);
        return room;
    }
}