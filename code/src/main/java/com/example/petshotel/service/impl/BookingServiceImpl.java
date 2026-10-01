package com.example.petshotel.service.impl;

import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.domain.entity.BookingPet;
import com.example.petshotel.domain.entity.BookingExtraService;
import com.example.petshotel.domain.entity.ExtraService;
import com.example.petshotel.domain.entity.Pet;
import com.example.petshotel.domain.entity.Promotion;
import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.domain.entity.User;

import com.example.petshotel.domain.enums.BookingStatus;
import com.example.petshotel.domain.enums.RoomStatus;
import com.example.petshotel.domain.enums.PaymentStatus;

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
import com.example.petshotel.repository.BookingPetRepository;

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
import com.example.petshotel.exception.PetNotAvailableException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.time.temporal.ChronoUnit;
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

    private static final List<BookingStatus>
            PET_RESERVING_STATUSES = List.of(
                    BookingStatus.PENDING,
                    BookingStatus.CONFIRMED,
                    BookingStatus.CHECKED_IN
            );

    private final BookingRepository bookingRepository;
    private final BookingPetRepository bookingPetRepository;
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
    @Transactional(readOnly = true)
    public BookingPriceResponse previewPrice(CreateBookingRequest request) {
        return prepareBooking(request, false).price();
    }

    @Override
    public BookingResponse createBooking(CreateBookingRequest request) {
        PreparedBooking prepared = prepareBooking(request, true);
        return bookingMapper.toResponse(
                bookingRepository.save(prepared.booking())
        );
    }

    private PreparedBooking prepareBooking(
            CreateBookingRequest request, boolean creating) {

        validateCreateRequest(request);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", request.getUserId()));

        Room room = (creating
                ? roomRepository.findByIdForUpdate(request.getRoomId())
                : roomRepository.findById(request.getRoomId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room", request.getRoomId()));

        if (room.getStatus() != RoomStatus.ACTIVE) {
            throw new RoomNotAvailableException(
                    room.getId(), request.getCheckInDate(),
                    request.getCheckOutDate());
        }

        List<Pet> pets = creating
        ? petRepository.findAllByIdForUpdate(
                request.getPetIds()
        )
        : petRepository.findAllById(
                request.getPetIds()
        );

        Set<Long> foundPetIds = pets.stream()
                .map(pet -> pet.getId()).collect(Collectors.toSet());

        for (Long petId : request.getPetIds()) {
            if (!foundPetIds.contains(petId)) {
                throw new ResourceNotFoundException("Pet", petId);
            }
        }

        validatePets(user, pets);

        if (pets.size() > room.getCapacity()) {
            throw new IllegalArgumentException(
                    "Number of pets exceeds room capacity");
        }

        // Preview is only a quote, not a reservation.
        // Preview is only a quote, not a reservation.
        if (creating) {
        List<Long> overlappingPetIds =
                bookingPetRepository.findOverlappingPetIds(
                        request.getPetIds(),
                        request.getCheckInDate(),
                        request.getCheckOutDate(),
                        PET_RESERVING_STATUSES
                );

        if (!overlappingPetIds.isEmpty()) {
                throw new PetNotAvailableException(
                        overlappingPetIds,
                        request.getCheckInDate(),
                        request.getCheckOutDate()
                );
        }

        availabilityService.checkRoomAvailable(
                room.getId(),
                request.getCheckInDate(),
                request.getCheckOutDate(),
                pets.size()
        );
        }

        // Transient graph only: preview does not save any of these objects.
        Booking booking = Booking.builder()
                .user(user)
                .room(room)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .status(BookingStatus.PENDING)
                .build();

        pets.forEach(booking::addPet);

        Map<Long, BookingPet> bookingPetsByPetId =
                booking.getBookingPets().stream().collect(Collectors.toMap(
                        item -> item.getPet().getId(), item -> item));

        addSelectedServices(booking, request.getServicePetIds(), bookingPetsByPetId);

        Promotion promotion = null;
        if (request.getPromotionId() != null) {
            promotion = promotionRepository.findById(request.getPromotionId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Promotion", request.getPromotionId()));
        }

        int nights = Math.toIntExact(ChronoUnit.DAYS.between(
                request.getCheckInDate(), request.getCheckOutDate()));

        BookingPriceResponse price = pricingService.calculate(new PricingContext(
                room, pets.size(), nights,
                request.getCheckInDate(), request.getCheckOutDate(),
                booking.getExtraServices(), promotion));

        booking.setRoomAmount(price.basePrice());
        booking.setServiceAmount(price.extraServicesPrice());
        booking.setSurchargeAmount(price.holidaySurcharge());
        booking.setDiscountAmount(price.discountAmount());
        booking.setTotalPrice(price.totalPrice());
        booking.setPromotion(promotion);
        booking.setPromotionName(promotion != null ? promotion.getName() : null);

        return new PreparedBooking(booking, price);
    }

    private void addSelectedServices(
            Booking booking,
            Map<Long, List<Long>> servicePetIds,
            Map<Long, BookingPet> bookingPetsByPetId) {

        if (servicePetIds == null) return;

        for (Map.Entry<Long, List<Long>> entry : servicePetIds.entrySet()) {
            Long serviceId = entry.getKey();
            List<Long> recipients = entry.getValue();

            if (serviceId == null || recipients == null) {
                throw new IllegalArgumentException(
                        "Service ID and recipient list are required");
            }

            if (recipients.isEmpty()) continue;

            Set<Long> uniqueRecipients = new HashSet<>();
            for (Long petId : recipients) {
                if (petId == null || !bookingPetsByPetId.containsKey(petId)) {
                    throw new IllegalArgumentException(
                            "Service recipient must be a pet in this booking");
                }
                if (!uniqueRecipients.add(petId)) {
                    throw new IllegalArgumentException(
                            "Duplicate service recipient is not allowed");
                }
            }

            ExtraService extra = extraServiceRepository.findById(serviceId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Extra service", serviceId));

            if (!Boolean.TRUE.equals(extra.getActive())
                    || extra.getPrice() == null
                    || extra.getPrice().signum() < 0) {
                throw new IllegalArgumentException(
                        "Extra service is unavailable: " + serviceId);
            }

            for (Long petId : recipients) {
                BookingPet bookingPet = bookingPetsByPetId.get(petId);

                if (bookingPet.getBooking() != booking
                        || !booking.getBookingPets().contains(bookingPet)) {
                    throw new IllegalStateException(
                            "Service recipient belongs to another booking");
                }

                BookingExtraService selected = new BookingExtraService();
                selected.setBookingPet(bookingPet);
                selected.setExtraService(extra);
                selected.setQuantity(1);
                selected.setUnitPrice(extra.getPrice());
                selected.setTotalPrice(extra.getPrice());

                booking.addExtraService(selected);
            }
        }
    }

    private record PreparedBooking(
            Booking booking, BookingPriceResponse price) {
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

        Booking booking = findBookingForUpdate(id);

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

        Booking booking = findBookingForUpdate(id);

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

        Booking booking = findBookingForUpdate(id);

        BookingState state = getState(booking.getStatus());

        state.checkOut(booking);

        Booking savedBooking = bookingRepository.save(booking);

        return bookingMapper.toResponse(savedBooking);
    }

    @Override
    public BookingResponse cancelBooking(Long id) {

        Booking booking = findBookingForUpdate(id);

        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
        throw new IllegalStateException(
                "Paid bookings cannot be cancelled through this operation"
        );
      }

        BookingState state = getState(booking.getStatus());
        state.cancel(booking);

        Booking savedBooking = bookingRepository.save(booking);

        return bookingMapper.toResponse(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId)
                .stream()
                .map(bookingMapper::toResponse)
                .toList();
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

    private Booking findBookingForUpdate(Long id) {
    return bookingRepository.findByIdForUpdate(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Booking", id
            ));
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

        if (request.getPetIds().stream().anyMatch(Objects::isNull)) {
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
