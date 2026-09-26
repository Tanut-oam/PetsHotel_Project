package com.example.petshotel.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.domain.enums.RoomStatus;
import com.example.petshotel.exception.RoomNotAvailableException;
import com.example.petshotel.repository.BookingPetRepository;
import com.example.petshotel.repository.RoomRepository;
import com.example.petshotel.service.impl.AvailabilityServiceImpl;

class AvailabilityServiceTest {

    private RoomRepository roomRepository;
    private BookingPetRepository bookingPetRepository;
    private AvailabilityServiceImpl availabilityService;

    private final LocalDate checkIn = LocalDate.of(2026, 10, 1);
    private final LocalDate checkOut = LocalDate.of(2026, 10, 3);

    @BeforeEach
    void setUp() {
        roomRepository = mock(RoomRepository.class);
        bookingPetRepository = mock(BookingPetRepository.class);
        availabilityService = new AvailabilityServiceImpl(roomRepository, bookingPetRepository);
    }

    private Room room(Long id, int capacity, RoomStatus status) {
        Room room = new Room();
        room.setId(id);
        room.setRoomNumber("R" + id);
        room.setName("Room " + id);
        room.setCapacity(capacity);
        room.setPricePerPetPerNight(new BigDecimal("500.00"));
        room.setStatus(status);
        return room;
    }

    private void occupied(Long roomId, long pets) {
        when(bookingPetRepository.countPetsInOverlappingBookings(eq(roomId), any(), any(), anyCollection()))
            .thenReturn(pets);
    }

    @Test
    void roomShouldBeAvailableWhenEnoughCapacityLeft() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room(1L, 3, RoomStatus.ACTIVE)));
        occupied(1L, 1);

        assertTrue(availabilityService.isRoomAvailable(1L, checkIn, checkOut, 2));
    }

    @Test
    void roomShouldNotBeAvailableWhenCapacityExceeded() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room(1L, 3, RoomStatus.ACTIVE)));
        occupied(1L, 2);

        assertFalse(availabilityService.isRoomAvailable(1L, checkIn, checkOut, 2));
    }

    @Test
    void roomUnderMaintenanceShouldNotBeAvailable() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room(1L, 3, RoomStatus.MAINTENANCE)));

        assertFalse(availabilityService.isRoomAvailable(1L, checkIn, checkOut, 1));
        verify(bookingPetRepository, never()).countPetsInOverlappingBookings(any(), any(), any(), anyCollection());
    }

    @Test
    void checkRoomAvailableShouldThrowWhenRoomIsFull() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room(1L, 2, RoomStatus.ACTIVE)));
        occupied(1L, 2);

        assertThrows(RoomNotAvailableException.class,
            () -> availabilityService.checkRoomAvailable(1L, checkIn, checkOut, 1));
    }

    @Test
    void shouldThrowWhenRoomNotFound() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> availabilityService.isRoomAvailable(99L, checkIn, checkOut, 1));
    }

    @Test
    void findAvailableRoomsShouldReturnOnlyRoomsWithSpace() {
        Room free = room(1L, 2, RoomStatus.ACTIVE);
        Room full = room(2L, 2, RoomStatus.ACTIVE);
        when(roomRepository.findByStatus(RoomStatus.ACTIVE)).thenReturn(List.of(free, full));
        occupied(1L, 0);
        occupied(2L, 2);

        List<Room> result = availabilityService.findAvailableRooms(checkIn, checkOut, 1);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void findAvailableRoomsShouldReturnEmptyWhenAllRoomsFull() {
        when(roomRepository.findByStatus(RoomStatus.ACTIVE)).thenReturn(List.of(room(1L, 1, RoomStatus.ACTIVE)));
        occupied(1L, 1);

        assertTrue(availabilityService.findAvailableRooms(checkIn, checkOut, 1).isEmpty());
    }
    
    @Test
    void shouldThrowWhenCheckOutEqualsCheckIn() {
        assertThrows(IllegalArgumentException.class,
            () -> availabilityService.isRoomAvailable(1L, checkIn, checkIn, 1));
    }

    @Test
    void shouldThrowWhenCheckOutBeforeCheckIn() {
        assertThrows(IllegalArgumentException.class,
            () -> availabilityService.findAvailableRooms(checkOut, checkIn, 1));
    }

    @Test
    void shouldThrowWhenPetCountIsZero() {
        assertThrows(IllegalArgumentException.class,
            () -> availabilityService.isRoomAvailable(1L, checkIn, checkOut, 0));
    }

    @Test
    void shouldThrowWhenDateIsNull() {
        assertThrows(IllegalArgumentException.class,
            () -> availabilityService.isRoomAvailable(1L, null, checkOut, 1));
    }
}