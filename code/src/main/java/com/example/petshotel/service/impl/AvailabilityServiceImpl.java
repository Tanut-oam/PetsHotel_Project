package com.example.petshotel.service.impl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.domain.enums.BookingStatus;
import com.example.petshotel.domain.enums.RoomStatus;
import com.example.petshotel.dto.response.RoomAvailabilityCalendarResponse;
import com.example.petshotel.dto.response.UnavailableDateRangeResponse;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.exception.RoomNotAvailableException;
import com.example.petshotel.repository.BookingRepository;
import com.example.petshotel.repository.RoomRepository;
import com.example.petshotel.service.AvailabilityService;

@Service
public class AvailabilityServiceImpl implements AvailabilityService {

    // สถานะที่ยังครองห้องอยู่
    // CANCELLED และ CHECKED_OUT ไม่นับว่าครองห้อง
    private static final List<BookingStatus> ACTIVE_STATUSES = List.of(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED,
            BookingStatus.CHECKED_IN
    );

    private static final long MAX_CALENDAR_DAYS = 366;

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public AvailabilityServiceImpl(
            RoomRepository roomRepository,
            BookingRepository bookingRepository) {

        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isRoomAvailable(
            Long roomId,
            LocalDate checkIn,
            LocalDate checkOut,
            int petCount) {

        validate(checkIn, checkOut, petCount);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Room",
                                roomId
                        )
                );

        return hasSpace(
                room,
                checkIn,
                checkOut,
                petCount
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<Room> findAvailableRooms(
            LocalDate checkIn,
            LocalDate checkOut,
            int petCount) {

        validate(checkIn, checkOut, petCount);

        List<Room> availableRooms = new ArrayList<>();

        for (Room room :
                roomRepository.findByStatus(RoomStatus.ACTIVE)) {

            if (hasSpace(
                    room,
                    checkIn,
                    checkOut,
                    petCount
            )) {
                availableRooms.add(room);
            }
        }

        return availableRooms;
    }

    @Transactional(readOnly = true)
        @Override
        public void checkRoomAvailable(
                Long roomId,
                LocalDate checkIn,
                LocalDate checkOut,
                int petCount) {

        validate(checkIn, checkOut, petCount);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Room", roomId)
                );

        if (room.getStatus() != RoomStatus.ACTIVE) {
                throw new RoomNotAvailableException(
                        roomId,
                        checkIn,
                        checkOut
                );
        }

        if (petCount > room.getCapacity()) {
                throw new IllegalArgumentException(
                        "ห้องนี้รองรับสัตว์เลี้ยงได้สูงสุด "
                                + room.getCapacity()
                                + " ตัว กรุณาลดจำนวนสัตว์เลี้ยงหรือเลือกห้องอื่น"
                );
        }

        if (bookingRepository.existsOverlappingBooking(
                roomId,
                checkIn,
                checkOut,
                ACTIVE_STATUSES)) {

                throw RoomNotAvailableException.alreadyBooked(
                        checkIn,
                        checkOut
                );
        }
        }

    @Transactional(readOnly = true)
    @Override
    public RoomAvailabilityCalendarResponse
            getRoomAvailabilityCalendar(
                    Long roomId,
                    LocalDate fromDate,
                    LocalDate toDate) {

        validateCalendarRange(
                roomId,
                fromDate,
                toDate
        );

        roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Room",
                                roomId
                        )
                );

        List<UnavailableDateRangeResponse> unavailableRanges =
                bookingRepository
                        .findOverlappingBookingsByRoom(
                                roomId,
                                fromDate,
                                toDate,
                                ACTIVE_STATUSES
                        )
                        .stream()
                        .map(booking ->
                                new UnavailableDateRangeResponse(
                                        booking.getCheckInDate(),
                                        booking.getCheckOutDate()
                                )
                        )
                        .toList();

        return new RoomAvailabilityCalendarResponse(
                roomId,
                fromDate,
                toDate,
                unavailableRanges
        );
    }

    /*
     * กฎหนึ่งห้องต่อหนึ่งการจอง:
     * ห้องต้องเปิดใช้งาน รองรับจำนวนสัตว์ได้
     * และไม่มีการจองอื่นทับช่วงวัน
     */
    private boolean hasSpace(
            Room room,
            LocalDate checkIn,
            LocalDate checkOut,
            int petCount) {

        if (room.getStatus() != RoomStatus.ACTIVE) {
            return false;
        }

        if (petCount > room.getCapacity()) {
            return false;
        }

        return !bookingRepository.existsOverlappingBooking(
                room.getId(),
                checkIn,
                checkOut,
                ACTIVE_STATUSES
        );
    }

    private void validate(
            LocalDate checkIn,
            LocalDate checkOut,
            int petCount) {

        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException(
                    "วันที่ Check-in และ Check-out ต้องระบุ"
            );
        }

        if (!checkIn.isBefore(checkOut)) {
            throw new IllegalArgumentException(
                    "วันที่ Check-out ต้องอยู่หลังวันที่ Check-in"
            );
        }

        if (petCount < 1) {
            throw new IllegalArgumentException(
                    "จำนวนสัตว์เลี้ยงต้องอย่างน้อย 1 ตัว"
            );
        }
    }

    private void validateCalendarRange(
            Long roomId,
            LocalDate fromDate,
            LocalDate toDate) {

        if (roomId == null) {
            throw new IllegalArgumentException(
                    "ต้องระบุห้องพัก"
            );
        }

        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException(
                    "ต้องระบุช่วงวันที่ของปฏิทิน"
            );
        }

        if (!fromDate.isBefore(toDate)) {
            throw new IllegalArgumentException(
                    "วันสิ้นสุดปฏิทินต้องอยู่หลังวันเริ่มต้น"
            );
        }

        long requestedDays =
                ChronoUnit.DAYS.between(
                        fromDate,
                        toDate
                );

        if (requestedDays > MAX_CALENDAR_DAYS) {
            throw new IllegalArgumentException(
                    "สามารถดูปฏิทินล่วงหน้าได้ไม่เกิน 1 ปี"
            );
        }
    }
}