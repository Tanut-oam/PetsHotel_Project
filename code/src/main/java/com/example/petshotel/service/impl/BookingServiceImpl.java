package com.example.petshotel.service.impl;

import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.domain.entity.BookingExtraService;
import com.example.petshotel.domain.entity.BookingPet;
import com.example.petshotel.domain.entity.ExtraService;
import com.example.petshotel.domain.entity.Pet;
import com.example.petshotel.domain.entity.Promotion;
import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.domain.entity.User;

import com.example.petshotel.domain.enums.BookingStatus;
import com.example.petshotel.domain.enums.RoomStatus;

import com.example.petshotel.dto.request.CreateBookingRequest;
import com.example.petshotel.dto.response.BookingResponse;

import com.example.petshotel.pricing.PricingContext;

import com.example.petshotel.repository.BookingExtraServiceRepository;
import com.example.petshotel.repository.BookingRepository;
import com.example.petshotel.repository.ExtraServiceRepository;
import com.example.petshotel.repository.PetRepository;
import com.example.petshotel.repository.PromotionRepository;
import com.example.petshotel.repository.RoomRepository;
import com.example.petshotel.repository.UserRepository;

import com.example.petshotel.service.BookingService;
import com.example.petshotel.service.PricingService;

import com.example.petshotel.state.BookingState;
import com.example.petshotel.state.CancelledState;
import com.example.petshotel.state.CheckedInState;
import com.example.petshotel.state.CheckedOutState;
import com.example.petshotel.state.ConfirmedState;
import com.example.petshotel.state.PendingState;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final PetRepository petRepository;
    private final PricingService pricingService;
    
    // Dependencies ที่เพิ่มเข้ามาใหม่
    private final ExtraServiceRepository extraServiceRepository;
    private final BookingExtraServiceRepository bookingExtraServiceRepository;
    private final PromotionRepository promotionRepository;

    // =========================================================
    // CREATE BOOKING
    // =========================================================

    @Override
    public BookingResponse createBooking(CreateBookingRequest request) {

        validateCreateRequest(request);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + request.getUserId()
                        )
                );

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Room not found: " + request.getRoomId()
                        )
                );

        // ห้องต้องเปิดใช้งาน
        if (room.getStatus() != RoomStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Room is not available for booking"
            );
        }

        // ดึงสัตว์ทั้งหมดจาก petIds
        List<Pet> pets = request.getPetIds()
                .stream()
                .map(petId -> petRepository.findById(petId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Pet not found: " + petId
                                )
                        ))
                .toList();

        validatePets(user, pets);

        // จำนวนสัตว์ต้องไม่เกิน capacity ของห้อง
        if (pets.size() > room.getCapacity()) {
            throw new IllegalArgumentException(
                    "Number of pets exceeds room capacity"
            );
        }

        // ตรวจว่าห้องถูกจองทับช่วงเวลานี้หรือไม่
        if (!isRoomAvailable(
                room.getId(),
                request.getCheckInDate(),
                request.getCheckOutDate())) {

            throw new IllegalStateException(
                    "Room is not available for selected dates"
            );
        }

        // จำนวนคืน
        int nights = Math.toIntExact(ChronoUnit.DAYS.between(
                request.getCheckInDate(),
                request.getCheckOutDate()
        ));

        List<BookingExtraService> selectedServices = new ArrayList<>();

        if (request.getExtraServiceQuantities() != null) {
            for (Map.Entry<Long, Integer> entry
                    : request.getExtraServiceQuantities().entrySet()) {

                Long serviceId = entry.getKey();
                Integer quantity = entry.getValue();

                if (serviceId == null || quantity == null || quantity <= 0) {
                    throw new IllegalArgumentException(
                            "Extra service ID and positive quantity are required");
                }

                ExtraService extraService = extraServiceRepository.findById(serviceId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Extra service not found: " + serviceId));

                if (!Boolean.TRUE.equals(extraService.getActive())
                        || extraService.getPrice() == null
                        || extraService.getPrice().signum() < 0) {
                    throw new IllegalArgumentException(
                            "Extra service is unavailable: " + serviceId);
                }

                BookingExtraService selected = new BookingExtraService();
                selected.setExtraService(extraService);
                selected.setQuantity(quantity);
                selected.setUnitPrice(extraService.getPrice());
                selected.setTotalPrice(
                        extraService.getPrice()
                                .multiply(BigDecimal.valueOf(quantity))
                );

                selectedServices.add(selected);
            }
        }

        // --- เพิ่มโค้ดค้นหา Promotion ตรงนี้ ---
        Promotion promotion = null;
        if (request.getPromotionId() != null) {
            promotion = promotionRepository.findById(request.getPromotionId())
                    .orElseThrow(() -> new IllegalArgumentException("Promotion not found: " + request.getPromotionId()));
        }
        // ------------------------------------

        // คำนวณราคาโดยส่ง selectedServices และ promotion เข้าไปใน PricingContext
        PricingContext pricingContext = new PricingContext(
                room,
                pets.size(),
                nights,
                request.getCheckInDate(),
                request.getCheckOutDate(),
                selectedServices,
                promotion // <-- เปลี่ยนจาก null เป็น promotion ตัวที่เราเพิ่งค้นหามา
        );

        BigDecimal totalPrice =
                pricingService.calculate(pricingContext).totalPrice();

        // สร้าง Booking หลัก
        Booking booking = Booking.builder()
                .user(user)
                .room(room)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .status(BookingStatus.PENDING)
                .totalPrice(totalPrice)
                .promotion(promotion) // <-- เพิ่มโปรโมชั่นเข้าไปผูกกับ Booking ด้วย
                .build();

        // สร้าง BookingPet เพื่อเชื่อม booking กับสัตว์แต่ละตัว
        List<BookingPet> bookingPets = pets.stream()
                .map(pet -> BookingPet.builder()
                        .booking(booking)
                        .pet(pet)
                        .build())
                .toList();

        booking.getBookingPets().addAll(bookingPets);

        // ผูก BookingExtraService กับ Booking หลัก
        if (!selectedServices.isEmpty()) {
            selectedServices.forEach(service -> service.setBooking(booking));
            booking.getExtraServices().addAll(selectedServices);
        }

        Booking savedBooking = bookingRepository.save(booking);

        return toResponse(savedBooking);
    }

    // =========================================================
    // GET BOOKING
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id) {

        Booking booking = findBooking(id);

        return toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {

        return bookingRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // STATE CHANGES
    // =========================================================

    @Override
    public BookingResponse confirmBooking(Long id) {

        Booking booking = findBooking(id);

        BookingState state = getState(booking.getStatus());

        state.confirm(booking);

        Booking savedBooking = bookingRepository.save(booking);

        return toResponse(savedBooking);
    }

    @Override
    public BookingResponse checkIn(Long id) {

        Booking booking = findBooking(id);

        BookingState state = getState(booking.getStatus());

        state.checkIn(booking);

        Booking savedBooking = bookingRepository.save(booking);

        return toResponse(savedBooking);
    }

    @Override
    public BookingResponse checkOut(Long id) {

        Booking booking = findBooking(id);

        BookingState state = getState(booking.getStatus());

        state.checkOut(booking);

        Booking savedBooking = bookingRepository.save(booking);

        return toResponse(savedBooking);
    }

    @Override
    public BookingResponse cancelBooking(Long id) {

        Booking booking = findBooking(id);

        BookingState state = getState(booking.getStatus());

        state.cancel(booking);

        Booking savedBooking = bookingRepository.save(booking);

        return toResponse(savedBooking);
    }

    // =========================================================
    // PRIVATE HELPER METHODS
    // =========================================================

    private Booking findBooking(Long id) {

        return bookingRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Booking not found: " + id
                        )
                );
    }

    private void validateCreateRequest(CreateBookingRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Booking request must not be null"
            );
        }

        if (request.getUserId() == null) {
            throw new IllegalArgumentException(
                    "User ID is required"
            );
        }

        if (request.getRoomId() == null) {
            throw new IllegalArgumentException(
                    "Room ID is required"
            );
        }

        if (request.getPetIds() == null
                || request.getPetIds().isEmpty()) {

            throw new IllegalArgumentException(
                    "At least one pet is required"
            );
        }

        if (request.getCheckInDate() == null
                || request.getCheckOutDate() == null) {

            throw new IllegalArgumentException(
                    "Check-in and check-out dates are required"
            );
        }

        if (!request.getCheckOutDate()
                .isAfter(request.getCheckInDate())) {

            throw new IllegalArgumentException(
                    "Check-out date must be after check-in date"
            );
        }
    }

    private void validatePets(User user, List<Pet> pets) {

        for (Pet pet : pets) {

            if (!Boolean.TRUE.equals(pet.getActive())) {
                throw new IllegalStateException(
                        "Pet is inactive: " + pet.getId()
                );
            }

            // สัตว์ที่นำมาจองต้องเป็นของ user คนนี้
            if (pet.getOwner() == null
                    || !pet.getOwner().getId().equals(user.getId())) {

                throw new IllegalArgumentException(
                        "Pet " + pet.getId()
                                + " does not belong to user "
                                + user.getId()
                );
            }
        }
    }

    private boolean isRoomAvailable(
            Long roomId,
            java.time.LocalDate checkIn,
            java.time.LocalDate checkOut) {

        List<Booking> roomBookings =
                bookingRepository.findByRoomId(roomId);

        return roomBookings.stream()
                .filter(booking ->
                        booking.getStatus() != BookingStatus.CANCELLED
                                && booking.getStatus()
                                != BookingStatus.CHECKED_OUT
                )
                .noneMatch(booking ->
                        checkIn.isBefore(booking.getCheckOutDate())
                                &&
                        checkOut.isAfter(booking.getCheckInDate())
                );
    }

    private BookingState getState(BookingStatus status) {

        return switch (status) {

            case PENDING ->
                    new PendingState();

            case CONFIRMED ->
                    new ConfirmedState();

            case CHECKED_IN ->
                    new CheckedInState();

            case CHECKED_OUT ->
                    new CheckedOutState();

            case CANCELLED ->
                    new CancelledState();
        };
    }

    private BookingResponse toResponse(Booking booking) {

        List<Long> petIds = booking.getBookingPets()
                .stream()
                .map(bookingPet ->
                        bookingPet.getPet().getId())
                .toList();

        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .roomId(booking.getRoom().getId())
                .petIds(petIds)
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .status(booking.getStatus())
                .totalPrice(booking.getTotalPrice())
                .build();
    }
}