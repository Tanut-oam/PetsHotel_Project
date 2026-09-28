package com.example.petshotel.service.impl;

import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.domain.entity.BookingExtraService;
import com.example.petshotel.domain.entity.ExtraService;
import com.example.petshotel.domain.entity.Pet;
import com.example.petshotel.domain.entity.Promotion;
import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.domain.entity.User;

import com.example.petshotel.domain.enums.BookingStatus;
import com.example.petshotel.domain.enums.RoomStatus;

import com.example.petshotel.dto.request.CreateBookingRequest;
import com.example.petshotel.dto.response.BookingResponse;
import com.example.petshotel.dto.response.BookingPriceResponse;

import com.example.petshotel.pricing.PricingContext;

import com.example.petshotel.repository.BookingRepository;
import com.example.petshotel.repository.ExtraServiceRepository;
import com.example.petshotel.repository.PetRepository;
import com.example.petshotel.repository.PromotionRepository;
import com.example.petshotel.repository.RoomRepository;
import com.example.petshotel.repository.UserRepository;

import com.example.petshotel.service.BookingService;
import com.example.petshotel.service.PricingService;
import com.example.petshotel.service.AvailabilityService;

import com.example.petshotel.state.BookingState;
import com.example.petshotel.state.CancelledState;
import com.example.petshotel.state.CheckedInState;
import com.example.petshotel.state.CheckedOutState;
import com.example.petshotel.state.ConfirmedState;
import com.example.petshotel.state.PendingState;

import com.example.petshotel.notification.BookingConfirmedEvent;

import com.example.petshotel.mapper.BookingMapper;

import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.exception.RoomNotAvailableException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Objects;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final PetRepository petRepository;
    private final PricingService pricingService;
    private final ExtraServiceRepository extraServiceRepository;
    private final PromotionRepository promotionRepository;
    private final AvailabilityService availabilityService;
    private final ApplicationEventPublisher eventPublisher;
    private final BookingMapper bookingMapper;

    // =========================================================
    // CREATE BOOKING
    // =========================================================

    @Override
    public BookingResponse createBooking(CreateBookingRequest request) {

        validateCreateRequest(request);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User" , request.getUserId()
                        )
                );

        Room room = roomRepository.findByIdForUpdate(request.getRoomId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Room" , request.getRoomId()
                        )
                );

        // ห้องต้องเปิดใช้งาน
        if (room.getStatus() != RoomStatus.ACTIVE) {
            throw new RoomNotAvailableException(
                    room.getId(),
                        request.getCheckInDate(),
                        request.getCheckOutDate()
            );
        }

        // ดึงสัตว์ทั้งหมดจาก petIds
        List<Pet> pets = petRepository.findAllById(request.getPetIds());

        // เก็บรหัสที่ค้นพบ เพื่อตรวจว่ามีรหัสใดหายไป
        Set<Long> foundPetIds = pets.stream()
                .map(pet -> pet.getId())
                .collect(Collectors.toSet());

        for (Long petId : request.getPetIds()) {
                if (!foundPetIds.contains(petId)) {
                        throw new ResourceNotFoundException("Pet", petId);
                }
        }

        validatePets(user, pets);

        // จำนวนสัตว์ต้องไม่เกิน capacity ของห้อง
        if (pets.size() > room.getCapacity()) {
            throw new IllegalArgumentException(
                    "Number of pets exceeds room capacity"
            );
        }

        // ตรวจว่าห้องถูกจองทับช่วงเวลานี้หรือไม่
        availabilityService.checkRoomAvailable(
                room.getId(),
                request.getCheckInDate(),
                request.getCheckOutDate(),
                pets.size()
        );

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
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Extra service" , serviceId));

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
                    .orElseThrow(() -> new ResourceNotFoundException("Promotion" , request.getPromotionId()));
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

        BookingPriceResponse price = pricingService.calculate(pricingContext);

        // สร้าง Booking หลัก
        Booking booking = Booking.builder()
                .user(user)
                .room(room)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .status(BookingStatus.PENDING)
                .roomAmount(price.basePrice())
                .serviceAmount(price.extraServicesPrice())
                .surchargeAmount(price.holidaySurcharge())
                .discountAmount(price.discountAmount())
                .totalPrice(price.totalPrice())
                .promotionName(promotion != null ? promotion.getName() : null)
                .promotion(promotion) 
                .build();

        // สร้าง BookingPet เพื่อเชื่อม booking กับสัตว์แต่ละตัว
        pets.forEach(pet -> booking.addPet(pet));

        // ผูก BookingExtraService กับ Booking หลัก
        selectedServices.forEach(service -> booking.addExtraService(service));

        Booking savedBooking = bookingRepository.save(booking);

        return bookingMapper.toResponse(savedBooking);
    }

    // =========================================================
    // GET BOOKING
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id) {

        Booking booking = findBooking(id);

        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {

        return bookingRepository.findAll()
                .stream()
                .map(bookingMapper::toResponse)
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

        eventPublisher.publishEvent(
        new BookingConfirmedEvent(this, savedBooking)
        );

        return bookingMapper.toResponse(savedBooking);
    }

    @Override
    public BookingResponse checkIn(Long id) {

        Booking booking = findBooking(id);

        BookingState state = getState(booking.getStatus());

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
        LocalDate today = LocalDate.now(
                ZoneId.of("Asia/Bangkok")
        );

        if (today.isBefore(booking.getCheckInDate())) {
            throw new IllegalStateException(
                    "Cannot check in before the scheduled check-in date"
            );
        }

        if (!today.isBefore(booking.getCheckOutDate())) {
            throw new IllegalStateException(
                    "Cannot check in on or after the scheduled check-out date"
            );
        }
      }

        state.checkIn(booking);

        Booking savedBooking = bookingRepository.save(booking);

        return bookingMapper.toResponse(savedBooking);
    }

    @Override
    public BookingResponse checkOut(Long id) {

        Booking booking = findBooking(id);

        BookingState state = getState(booking.getStatus());

        state.checkOut(booking);

        Booking savedBooking = bookingRepository.save(booking);

        return bookingMapper.toResponse(savedBooking);
    }

    @Override
    public BookingResponse cancelBooking(Long id) {

        Booking booking = findBooking(id);

        BookingState state = getState(booking.getStatus());

        state.cancel(booking);

        Booking savedBooking = bookingRepository.save(booking);

        return bookingMapper.toResponse(savedBooking);
    }

    // =========================================================
    // PRIVATE HELPER METHODS
    // =========================================================

    private Booking findBooking(Long id) {

        return bookingRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Booking" , id
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

        if (request.getPetIds().contains(null)) {
                throw new IllegalArgumentException(
                        "Pet ID must not be null"
                );
        }

        Set<Long> uniquePetIds = new HashSet<>(request.getPetIds());

        if (uniquePetIds.size() != request.getPetIds().size()) {
                throw new IllegalArgumentException(
                        "Duplicate pet IDs are not allowed"
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
                    || !Objects.equals(pet.getOwner().getId(), user.getId())) {

                throw new IllegalArgumentException(
                        "Pet " + pet.getId()
                                + " does not belong to user "
                                + user.getId()
                );
            }
        }
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

}