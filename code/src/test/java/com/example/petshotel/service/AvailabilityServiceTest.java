package com.example.petshotel.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.domain.enums.BookingStatus;
import com.example.petshotel.domain.enums.RoomStatus;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.exception.RoomNotAvailableException;
import com.example.petshotel.repository.BookingRepository;
import com.example.petshotel.repository.RoomRepository;
import com.example.petshotel.service.impl.AvailabilityServiceImpl;

class AvailabilityServiceTest {

    private RoomRepository roomRepository;
    private BookingRepository bookingRepository;
    private AvailabilityServiceImpl availabilityService;

    private final LocalDate checkIn = LocalDate.of(2026, 10, 1);
    private final LocalDate checkOut = LocalDate.of(2026, 10, 3);

    @BeforeEach
    void setUp() {
        roomRepository = mock(RoomRepository.class);
        bookingRepository = mock(BookingRepository.class);
        availabilityService = new AvailabilityServiceImpl(roomRepository, bookingRepository);
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

    private void booked(Long roomId, boolean hasOverlap) {
        when(bookingRepository.existsOverlappingBooking(eq(roomId), any(), any(), anyCollection()))
            .thenReturn(hasOverlap);
    }

    // ---------- กติกา 1 ห้อง = 1 การจอง ----------

    @Test
    void roomShouldBeAvailableWhenNoBookingAndWithinCapacity() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room(1L, 3, RoomStatus.ACTIVE)));
        booked(1L, false);

        assertTrue(availabilityService.isRoomAvailable(1L, checkIn, checkOut, 2));
    }

    @Test
    void roomShouldNotBeAvailableWhenAnyBookingOverlapsEvenWithCapacityLeft() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room(1L, 3, RoomStatus.ACTIVE)));
        booked(1L, true);

        assertFalse(availabilityService.isRoomAvailable(1L, checkIn, checkOut, 1));
    }

    @Test
    void roomShouldNotBeAvailableWhenPetsExceedCapacity() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room(1L, 3, RoomStatus.ACTIVE)));

        assertFalse(availabilityService.isRoomAvailable(1L, checkIn, checkOut, 4));
        verify(bookingRepository, never()).existsOverlappingBooking(any(), any(), any(), anyCollection());
    }

    @Test
    void roomUnderMaintenanceShouldNotBeAvailable() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room(1L, 3, RoomStatus.MAINTENANCE)));

        assertFalse(availabilityService.isRoomAvailable(1L, checkIn, checkOut, 1));
        verify(bookingRepository, never()).existsOverlappingBooking(any(), any(), any(), anyCollection());
    }

    @Test
    void inactiveRoomShouldNotBeAvailable() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room(1L, 3, RoomStatus.INACTIVE)));

        assertFalse(availabilityService.isRoomAvailable(1L, checkIn, checkOut, 1));
    }

    @SuppressWarnings("unchecked")
    @Test
    void cancelledAndCheckedOutBookingsShouldNotBlockRoom() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room(1L, 3, RoomStatus.ACTIVE)));
        booked(1L, false);

        availabilityService.isRoomAvailable(1L, checkIn, checkOut, 1);

        ArgumentCaptor<Collection<BookingStatus>> statuses = ArgumentCaptor.forClass(Collection.class);
        verify(bookingRepository).existsOverlappingBooking(eq(1L), eq(checkIn), eq(checkOut), statuses.capture());
        assertTrue(statuses.getValue().contains(BookingStatus.PENDING));
        assertTrue(statuses.getValue().contains(BookingStatus.CONFIRMED));
        assertTrue(statuses.getValue().contains(BookingStatus.CHECKED_IN));
        assertFalse(statuses.getValue().contains(BookingStatus.CANCELLED));
        assertFalse(statuses.getValue().contains(BookingStatus.CHECKED_OUT));
    }

    @Test
    void checkRoomAvailableShouldThrowWhenRoomIsBooked() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room(1L, 3, RoomStatus.ACTIVE)));
        booked(1L, true);

        RoomNotAvailableException ex = assertThrows(RoomNotAvailableException.class,
            () -> availabilityService.checkRoomAvailable(1L, checkIn, checkOut, 1));
        assertTrue(ex.getMessage().contains("Room 1 is not available"));
    }

    @Test
    void shouldThrowWhenRoomNotFound() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
            () -> availabilityService.isRoomAvailable(99L, checkIn, checkOut, 1));
        assertEquals("Room not found: 99", ex.getMessage());
    }

    // ---------- ค้นหาห้องว่าง ----------

    @Test
    void findAvailableRoomsShouldReturnOnlyUnbookedRooms() {
        Room free = room(1L, 2, RoomStatus.ACTIVE);
        Room booked = room(2L, 2, RoomStatus.ACTIVE);
        when(roomRepository.findByStatus(RoomStatus.ACTIVE)).thenReturn(List.of(free, booked));
        booked(1L, false);
        booked(2L, true);

        List<Room> result = availabilityService.findAvailableRooms(checkIn, checkOut, 1);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void findAvailableRoomsShouldSkipRoomsTooSmall() {
        Room small = room(1L, 1, RoomStatus.ACTIVE);
        Room big = room(2L, 4, RoomStatus.ACTIVE);
        when(roomRepository.findByStatus(RoomStatus.ACTIVE)).thenReturn(List.of(small, big));
        booked(2L, false);

        List<Room> result = availabilityService.findAvailableRooms(checkIn, checkOut, 3);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
    }

    @Test
    void findAvailableRoomsShouldReturnEmptyWhenAllRoomsBooked() {
        when(roomRepository.findByStatus(RoomStatus.ACTIVE)).thenReturn(List.of(room(1L, 3, RoomStatus.ACTIVE)));
        booked(1L, true);

        assertTrue(availabilityService.findAvailableRooms(checkIn, checkOut, 1).isEmpty());
    }

    // ---------- ตรวจข้อมูลที่ส่งเข้ามา ----------

    @Test
    void shouldThrowWhenCheckOutEqualsCheckIn() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> availabilityService.isRoomAvailable(1L, checkIn, checkIn, 1));
        assertEquals("วันที่ Check-out ต้องอยู่หลังวันที่ Check-in", ex.getMessage());
    }

    @Test
    void shouldThrowWhenCheckOutBeforeCheckIn() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> availabilityService.findAvailableRooms(checkOut, checkIn, 1));
        assertEquals("วันที่ Check-out ต้องอยู่หลังวันที่ Check-in", ex.getMessage());
    }

    @Test
    void shouldThrowWhenPetCountIsZero() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> availabilityService.isRoomAvailable(1L, checkIn, checkOut, 0));
        assertEquals("จำนวนสัตว์เลี้ยงต้องอย่างน้อย 1 ตัว", ex.getMessage());
    }

    @Test
    void shouldThrowWhenDateIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> availabilityService.isRoomAvailable(1L, null, checkOut, 1));
        assertEquals("วันที่ Check-in และ Check-out ต้องระบุ", ex.getMessage());
    }
}