package com.example.petshotel.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.domain.enums.BookingStatus;
import com.example.petshotel.domain.enums.RoomStatus;
import com.example.petshotel.repository.BookingPetRepository;
import com.example.petshotel.repository.RoomRepository;
import com.example.petshotel.service.AvailabilityService;

@Service
public class AvailabilityServiceImpl implements AvailabilityService {

    // สถานะที่ยังครองห้องอยู่ (CANCELLED / CHECKED_OUT ไม่นับ)
    private static final List<BookingStatus> ACTIVE_STATUSES = List.of(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED,
            BookingStatus.CHECKED_IN
    );

    private final RoomRepository roomRepository;
    private final BookingPetRepository bookingPetRepository;

    public AvailabilityServiceImpl(RoomRepository roomRepository, BookingPetRepository bookingPetRepository) {
        this.roomRepository = roomRepository;
        this.bookingPetRepository = bookingPetRepository;
    }

    @Transactional(readOnly = true)
    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut, int petCount) {
        validate(checkIn, checkOut, petCount);
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));
        return hasSpace(room, checkIn, checkOut, petCount);
    }

    @Transactional(readOnly = true)
    public List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut, int petCount) {
        validate(checkIn, checkOut, petCount);
        List<Room> availableRooms = new ArrayList<>();
        for (Room room : roomRepository.findByStatus(RoomStatus.ACTIVE)) {
            if (hasSpace(room, checkIn, checkOut, petCount)) {
                availableRooms.add(room);
            }
        }
        return availableRooms;
    } 

    private boolean hasSpace(Room room, LocalDate checkIn, LocalDate checkOut, int petCount) {
        if (room.getStatus() != RoomStatus.ACTIVE) {
            return false;
        }
        long occupied = bookingPetRepository.countPetsInOverlappingBookings(room.getId(), checkIn, checkOut, ACTIVE_STATUSES);
        return occupied + petCount <= room.getCapacity();
    }

    private void validate(LocalDate checkIn, LocalDate checkOut, int petCount) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("วันที่ Check-in และ Check-out ต้องระบุ");
        }
        if (!checkIn.isBefore(checkOut)) {
            throw new IllegalArgumentException("วันที่ Check-out ต้องอยู่หลังวันที่ Check-in");
        }
        if (petCount < 1) {
            throw new IllegalArgumentException("จำนวนสัตว์เลี้ยงต้องอย่างน้อย 1 ตัว");
        }
    }
                                                                                                                                                                                                                   
}