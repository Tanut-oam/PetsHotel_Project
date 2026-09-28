package com.example.petshotel.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.domain.enums.BookingStatus;
import com.example.petshotel.domain.enums.RoomStatus;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.exception.RoomNotAvailableException;
import com.example.petshotel.repository.BookingRepository;
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
    private final BookingRepository bookingRepository;

    public AvailabilityServiceImpl(RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut, int petCount) {
        validate(checkIn, checkOut, petCount);
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room", roomId));
        return hasSpace(room, checkIn, checkOut, petCount);
    }

    @Transactional(readOnly = true)
    @Override
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

    @Transactional(readOnly = true)
    @Override
    public void checkRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut, int petCount) {
        if (!isRoomAvailable(roomId, checkIn, checkOut, petCount)) {
            throw new RoomNotAvailableException(roomId, checkIn, checkOut);
        }
    }

    // 1 ห้อง = 1 การจอง: ห้องต้องเปิดใช้งาน รับจำนวนสัตว์ได้ และไม่มีการจองทับช่วงวัน
    private boolean hasSpace(Room room, LocalDate checkIn, LocalDate checkOut, int petCount) {
        if (room.getStatus() != RoomStatus.ACTIVE) {
            return false;
        }
        if (petCount > room.getCapacity()) {
            return false;
        }
        return !bookingRepository.existsOverlappingBooking(room.getId(), checkIn, checkOut, ACTIVE_STATUSES);
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