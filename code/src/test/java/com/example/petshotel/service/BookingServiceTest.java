package com.example.petshotel.service;

import com.example.petshotel.domain.entity.*;
import com.example.petshotel.domain.enums.BookingStatus;
import com.example.petshotel.domain.enums.PaymentStatus;
import com.example.petshotel.domain.enums.RoomStatus;
import com.example.petshotel.dto.request.CreateBookingRequest;
import com.example.petshotel.dto.response.BookingPriceResponse;
import com.example.petshotel.dto.response.BookingResponse;
import com.example.petshotel.dto.response.PetAvailabilityResponse;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.exception.RoomNotAvailableException;
import com.example.petshotel.exception.PetNotAvailableException;
import com.example.petshotel.mapper.BookingMapper;
import com.example.petshotel.notification.BookingConfirmedEvent;
import com.example.petshotel.pricing.PricingContext;
import com.example.petshotel.repository.*;
import com.example.petshotel.service.impl.BookingServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    private BookingRepository bookingRepository;
    private BookingPetRepository bookingPetRepository;
    private UserRepository userRepository;
    private RoomRepository roomRepository;
    private PetRepository petRepository;
    private PricingService pricingService;
    private ExtraServiceRepository extraServiceRepository;
    private PromotionRepository promotionRepository;
    private AvailabilityService availabilityService;
    private ApplicationEventPublisher eventPublisher;
    private BookingServiceImpl bookingService;

    private User user;
    private Room room;
    private Pet pet;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        bookingPetRepository = mock(BookingPetRepository.class);
        userRepository = mock(UserRepository.class);
        roomRepository = mock(RoomRepository.class);
        petRepository = mock(PetRepository.class);
        pricingService = mock(PricingService.class);
        extraServiceRepository = mock(ExtraServiceRepository.class);
        promotionRepository = mock(PromotionRepository.class);
        availabilityService = mock(AvailabilityService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        bookingService = new BookingServiceImpl(
                bookingRepository,
                bookingPetRepository,
                userRepository,
                roomRepository,
                petRepository,
                pricingService,
                extraServiceRepository,
                promotionRepository,
                availabilityService,
                eventPublisher,
                new BookingMapper()
        );

        user = new User();
        user.setId(10L);

        room = new Room();
        room.setId(20L);
        room.setStatus(RoomStatus.ACTIVE);
        room.setCapacity(2);
        room.setPricePerPetPerNight(new BigDecimal("500.00"));

        pet = new Pet();
        pet.setId(30L);
        pet.setName("มอคอ");
        pet.setOwner(user);
        pet.setActive(true);
    }

    // ---------- PET AVAILABILITY ----------

        @Test
        void getPetAvailabilityShouldReturnOverlappingActivePets() {
        LocalDate checkInDate =
                LocalDate.of(2026, 10, 10);

        LocalDate checkOutDate =
                LocalDate.of(2026, 10, 12);

        when(
                petRepository
                        .findByOwner_IdAndActiveTrue(10L)
        ).thenReturn(List.of(pet));

        when(
                bookingPetRepository.findOverlappingPetIds(
                        eq(List.of(30L)),
                        eq(checkInDate),
                        eq(checkOutDate),
                        anyCollection()
                )
        ).thenReturn(List.of(30L));

        PetAvailabilityResponse response =
                bookingService.getPetAvailability(
                        10L,
                        checkInDate,
                        checkOutDate
                );

        assertEquals(
                checkInDate,
                response.checkInDate()
        );

        assertEquals(
                checkOutDate,
                response.checkOutDate()
        );

        assertEquals(
                List.of(30L),
                response.unavailablePetIds()
        );

        verify(
                petRepository
        ).findByOwner_IdAndActiveTrue(10L);
        }

        @Test
        void getPetAvailabilityShouldSkipQueryWhenUserHasNoActivePets() {
        LocalDate checkInDate =
                LocalDate.of(2026, 10, 10);

        LocalDate checkOutDate =
                LocalDate.of(2026, 10, 12);

        when(
                petRepository
                        .findByOwner_IdAndActiveTrue(10L)
        ).thenReturn(List.of());

        PetAvailabilityResponse response =
                bookingService.getPetAvailability(
                        10L,
                        checkInDate,
                        checkOutDate
                );

        assertTrue(
                response.unavailablePetIds().isEmpty()
        );

        verifyNoInteractions(bookingPetRepository);
        }

    // ---------- CREATE BOOKING ----------

    @Test
    void createBookingShouldSavePendingBookingWithPriceSnapshot() {
        CreateBookingRequest request = validRequest();
        prepareCreate();

        BookingResponse response = bookingService.createBooking(request);
        Booking saved = capturedBooking();

        assertEquals(BookingStatus.PENDING, saved.getStatus());
        assertEquals(PaymentStatus.UNPAID, saved.getPaymentStatus());
        assertEquals(new BigDecimal("1000.00"), saved.getRoomAmount());
        assertEquals(new BigDecimal("0.00"), saved.getServiceAmount());
        assertEquals(new BigDecimal("100.00"), saved.getSurchargeAmount());
        assertEquals(new BigDecimal("50.00"), saved.getDiscountAmount());
        assertEquals(new BigDecimal("1050.00"), saved.getTotalPrice());

        assertEquals(1, saved.getBookingPets().size());
        assertSame(pet, saved.getBookingPets().get(0).getPet());
        assertSame(saved, saved.getBookingPets().get(0).getBooking());
        assertTrue(saved.getExtraServices().isEmpty());
        assertNull(saved.getPromotion());
        assertNull(saved.getPromotionName());

        assertEquals(1L, response.getId());
        assertEquals(List.of(30L), response.getPetIds());
        assertEquals(new BigDecimal("1050.00"), response.getTotalPrice());

        ArgumentCaptor<PricingContext> captor =
                ArgumentCaptor.forClass(PricingContext.class);
        verify(pricingService).calculate(captor.capture());

        assertEquals(2, captor.getValue().getNights());
        assertEquals(1, captor.getValue().getPetCount());
        assertSame(room, captor.getValue().getRoom());

        verify(availabilityService).checkRoomAvailable(
                20L, request.getCheckInDate(), request.getCheckOutDate(), 1
        );
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void nullRequestShouldRejectBeforeDatabaseAccess() {
        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(null)
        );
        verifyNoInteractions(userRepository, roomRepository, bookingRepository);
    }

    @Test
    void duplicatePetIdsShouldRejectBeforeDatabaseAccess() {
        CreateBookingRequest request = validRequest();
        request.setPetIds(List.of(30L, 30L));

        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(request)
        );
        verifyNoInteractions(userRepository, roomRepository, bookingRepository);
    }

    @Test
    void nullPetIdShouldRejectBeforeDatabaseAccess() {
        CreateBookingRequest request = validRequest();
        request.setPetIds(Arrays.asList(30L, null));

        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(request)
        );
        verifyNoInteractions(userRepository, roomRepository, bookingRepository);
    }

    @Test
    void emptyPetIdsShouldRejectBeforeDatabaseAccess() {
        CreateBookingRequest request = validRequest();
        request.setPetIds(List.of());

        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(request)
        );
        verifyNoInteractions(userRepository, roomRepository, bookingRepository);
    }

    @Test
    void sameCheckInAndCheckOutShouldReject() {
        CreateBookingRequest request = validRequest();
        request.setCheckOutDate(request.getCheckInDate());

        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(request)
        );
        verifyNoInteractions(userRepository, roomRepository, bookingRepository);
    }

    @Test
    void missingUserShouldReject() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.createBooking(validRequest())
        );
        verifyNoInteractions(roomRepository, pricingService);
        verifyNoSave();
    }

    @Test
    void missingRoomShouldReject() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(roomRepository.findByIdForUpdate(20L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.createBooking(validRequest())
        );
        verifyNoSave();
    }

    @Test
    void inactiveRoomShouldReject() {
        prepareCreate();
        room.setStatus(RoomStatus.INACTIVE);

        assertThrows(
                RoomNotAvailableException.class,
                () -> bookingService.createBooking(validRequest())
        );
        verify(pricingService, never()).calculate(any());
        verifyNoSave();
    }

    @Test
    void missingPetShouldReject() {
        prepareCreate();
        when(petRepository.findAllByIdForUpdate(List.of(30L)))
                .thenReturn(List.of());

        assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.createBooking(validRequest())
        );
        verifyNoSave();
    }

    @Test
    void anotherUsersPetShouldReject() {
        prepareCreate();
        User anotherOwner = new User();
        anotherOwner.setId(99L);
        pet.setOwner(anotherOwner);

        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(validRequest())
        );
        verifyNoSave();
    }

    @Test
    void inactivePetShouldReject() {
        prepareCreate();
        pet.setActive(false);

        assertThrows(
                IllegalStateException.class,
                () -> bookingService.createBooking(validRequest())
        );
        verifyNoSave();
    }

    @Test
    void petWithOverlappingBookingShouldRejectBeforeRoomCheck() {
        prepareCreate();

        CreateBookingRequest request = validRequest();

        when(bookingPetRepository.findOverlappingPetIds(
                request.getPetIds(),
                request.getCheckInDate(),
                request.getCheckOutDate(),
                List.of(
                        BookingStatus.PENDING,
                        BookingStatus.CONFIRMED,
                        BookingStatus.CHECKED_IN
                )
        )).thenReturn(List.of(30L));

        PetNotAvailableException exception = assertThrows(
                PetNotAvailableException.class,
                () -> bookingService.createBooking(request)
        );

        assertTrue(
        exception.getMessage().contains("มอคอ")
        );

        assertTrue(
        exception.getMessage().contains(
                "10 ตุลาคม 2026"
        )
        );

        assertTrue(
                exception.getMessage().contains(
                        "12 ตุลาคม 2026"
                )
        );
        verifyNoInteractions(availabilityService);
        verify(pricingService, never()).calculate(any(PricingContext.class));
        verifyNoSave();
    }

    @Test
    void petCountExceedingCapacityShouldReject() {
        prepareCreate();
        room.setCapacity(1);

        Pet secondPet = new Pet();
        secondPet.setId(31L);
        secondPet.setOwner(user);
        secondPet.setActive(true);

        CreateBookingRequest request = validRequest();
        request.setPetIds(List.of(30L, 31L));

        when(petRepository.findAllByIdForUpdate(List.of(30L, 31L)))
                .thenReturn(List.of(pet, secondPet));

        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(request)
        );
        verifyNoInteractions(availabilityService);
        verifyNoSave();
    }

    @Test
    void unavailableRoomShouldStopBeforePricingAndSaving() {
        prepareCreate();
        CreateBookingRequest request = validRequest();

        doThrow(new RoomNotAvailableException(
                20L, request.getCheckInDate(), request.getCheckOutDate()
        )).when(availabilityService).checkRoomAvailable(
                20L, request.getCheckInDate(), request.getCheckOutDate(), 1
        );

        assertThrows(
                RoomNotAvailableException.class,
                () -> bookingService.createBooking(request)
        );
        verify(pricingService, never()).calculate(any());
        verifyNoSave();
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void extraServiceShouldKeepPriceSnapshotAndBookingReference() {
        prepareCreate();
        CreateBookingRequest request = validRequest();
        request.setServicePetIds(Map.of(40L, List.of(30L)));

        ExtraService extra = new ExtraService();
        extra.setId(40L);
        extra.setActive(true);
        extra.setPrice(new BigDecimal("300.00"));

        when(extraServiceRepository.findById(40L)).thenReturn(Optional.of(extra));
        when(pricingService.calculate(any(PricingContext.class)))
                .thenReturn(new BookingPriceResponse(
                        new BigDecimal("1000.00"), new BigDecimal("300.00"),
                        BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("1300.00")));

        bookingService.createBooking(request);
        Booking saved = capturedBooking();
        BookingExtraService selected = saved.getExtraServices().get(0);

        assertEquals(1, saved.getExtraServices().size());
        assertSame(saved, selected.getBooking());
        assertSame(saved.getBookingPets().get(0), selected.getBookingPet());
        assertSame(saved, selected.getBookingPet().getBooking());
        assertSame(pet, selected.getBookingPet().getPet());
        assertSame(extra, selected.getExtraService());
        assertEquals(Integer.valueOf(1), selected.getQuantity());
        assertEquals(new BigDecimal("300.00"), selected.getUnitPrice());
        assertEquals(new BigDecimal("300.00"), selected.getTotalPrice());

        extra.setPrice(new BigDecimal("350.00"));
        assertEquals(new BigDecimal("300.00"), selected.getUnitPrice());
        assertEquals(new BigDecimal("300.00"), selected.getTotalPrice());
    }

    @Test
    void serviceRecipientOutsideBookingShouldReject() {
        prepareCreate();
        CreateBookingRequest request = validRequest();
        request.setServicePetIds(Map.of(40L, List.of(999L)));
        assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(request));
        verifyNoInteractions(extraServiceRepository);
        verifyNoSave();
    }

    @Test
    void missingExtraServiceShouldReject() {
        prepareCreate();
        CreateBookingRequest request = validRequest();
        request.setServicePetIds(Map.of(40L, List.of(30L)));
        when(extraServiceRepository.findById(40L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.createBooking(request)
        );
        verifyNoSave();
    }

    @Test
    void promotionNameShouldRemainSnapshot() {
        prepareCreate();
        CreateBookingRequest request = validRequest();
        request.setPromotionId(50L);

        Promotion promotion = new Promotion();
        promotion.setId(50L);
        promotion.setName("SAVE20");
        promotion.setActive(true);

        when(promotionRepository.findById(50L))
                .thenReturn(Optional.of(promotion));

        bookingService.createBooking(request);
        Booking saved = capturedBooking();

        assertSame(promotion, saved.getPromotion());
        assertEquals("SAVE20", saved.getPromotionName());

        ArgumentCaptor<PricingContext> captor =
                ArgumentCaptor.forClass(PricingContext.class);
        verify(pricingService).calculate(captor.capture());
        assertSame(promotion, captor.getValue().getPromotion());

        promotion.setName("SAVE30");
        assertEquals("SAVE20", saved.getPromotionName());
    }

    @Test
    void missingPromotionShouldReject() {
        prepareCreate();
        CreateBookingRequest request = validRequest();
        request.setPromotionId(50L);
        when(promotionRepository.findById(50L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.createBooking(request)
        );
        verifyNoSave();
    }

    // ---------- STATE AND PAYMENT ----------

    @Test
    void confirmPendingBookingShouldPublishEvent() {
        Booking booking = existingBooking(BookingStatus.PENDING);
        prepareUpdate(booking);

        BookingResponse response = bookingService.confirmBooking(1L);

        assertEquals(BookingStatus.CONFIRMED, response.getStatus());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        verify(bookingRepository).save(booking);

        ArgumentCaptor<BookingConfirmedEvent> captor =
                ArgumentCaptor.forClass(BookingConfirmedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertSame(booking, captor.getValue().getBooking());
    }

    @Test
    void confirmCancelledBookingShouldRejectWithoutEvent() {
        Booking booking = existingBooking(BookingStatus.CANCELLED);
        prepareUpdate(booking);

        assertThrows(
                IllegalStateException.class,
                () -> bookingService.confirmBooking(1L)
        );
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        verifyNoSave();
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void cancelUnpaidConfirmedBookingShouldSucceed() {
        Booking booking = existingBooking(BookingStatus.CONFIRMED);
        prepareUpdate(booking);

        BookingResponse response = bookingService.cancelBooking(1L);

        assertEquals(BookingStatus.CANCELLED, response.getStatus());
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(PaymentStatus.UNPAID, booking.getPaymentStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void cancelPaidBookingShouldKeepOriginalData() {
        Booking booking = existingBooking(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setPaidAmount(new BigDecimal("1000.00"));
        booking.setPaidAt(LocalDate.of(2026, 10, 9).atTime(12, 0));
        var originalPaidAt = booking.getPaidAt();
        prepareUpdate(booking);

        assertThrows(
                IllegalStateException.class,
                () -> bookingService.cancelBooking(1L)
        );
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(PaymentStatus.PAID, booking.getPaymentStatus());
        assertEquals(new BigDecimal("1000.00"), booking.getPaidAmount());
        assertEquals(originalPaidAt, booking.getPaidAt());
        verifyNoSave();
    }

    @Test
    void cancelCheckedInBookingShouldReject() {
        Booking booking = existingBooking(BookingStatus.CHECKED_IN);
        prepareUpdate(booking);

        assertThrows(
                IllegalStateException.class,
                () -> bookingService.cancelBooking(1L)
        );
        assertEquals(BookingStatus.CHECKED_IN, booking.getStatus());
        verifyNoSave();
    }

    @Test
    void cancelMissingBookingShouldReturnNotFound() {
        when(bookingRepository.findByIdForUpdate(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.cancelBooking(99L)
        );
        verifyNoSave();
    }

    @Test
    void checkOutShouldNotChangePaymentStatus() {
        Booking booking = existingBooking(BookingStatus.CHECKED_IN);
        prepareUpdate(booking);

        BookingResponse response = bookingService.checkOut(1L);

        assertEquals(BookingStatus.CHECKED_OUT, response.getStatus());
        assertEquals(BookingStatus.CHECKED_OUT, booking.getStatus());
        assertEquals(PaymentStatus.UNPAID, booking.getPaymentStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void checkOutBeforeCheckInShouldReject() {
        Booking booking = existingBooking(BookingStatus.CONFIRMED);
        prepareUpdate(booking);

        assertThrows(
                IllegalStateException.class,
                () -> bookingService.checkOut(1L)
        );
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        verifyNoSave();
    }

    // ---------- CHECK-IN DATE BOUNDARIES ----------
    // Fix "today" only within each test; restore it automatically afterward.

    @Test
    void checkInBeforeScheduledDateShouldReject() {
        assertCheckInOn(LocalDate.of(2026, 10, 9), false);
    }

    @Test
    void checkInOnScheduledDateShouldSucceed() {
        assertCheckInOn(LocalDate.of(2026, 10, 10), true);
    }

    @Test
    void lateCheckInBeforeCheckOutDateShouldSucceed() {
        assertCheckInOn(LocalDate.of(2026, 10, 11), true);
    }

    @Test
    void checkInOnCheckOutDateShouldReject() {
        assertCheckInOn(LocalDate.of(2026, 10, 12), false);
    }

    @Test
    void checkInAfterCheckOutDateShouldReject() {
        assertCheckInOn(LocalDate.of(2026, 10, 13), false);
    }

    @Test
    void pendingBookingCannotCheckIn() {
        Booking booking = existingBooking(BookingStatus.PENDING);
        prepareUpdate(booking);

        assertThrows(
                IllegalStateException.class,
                () -> bookingService.checkIn(1L)
        );
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        verifyNoSave();
    }

    // ---------- READ ----------

    @Test
    void getBookingShouldUseReadQueryWithoutLock() {
        Booking booking = existingBooking(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        BookingResponse response = bookingService.getBookingById(1L);

        assertEquals(1L, response.getId());
        verify(bookingRepository).findById(1L);
        verify(bookingRepository, never()).findByIdForUpdate(anyLong());
        verifyNoSave();
    }

    // ---------- HELPERS ----------

    private CreateBookingRequest validRequest() {
        return CreateBookingRequest.builder()
                .userId(10L)
                .roomId(20L)
                .petIds(List.of(30L))
                .checkInDate(LocalDate.of(2026, 10, 10))
                .checkOutDate(LocalDate.of(2026, 10, 12))
                .build();
    }

    private void prepareCreate() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(roomRepository.findByIdForUpdate(20L))
                .thenReturn(Optional.of(room));
        when(petRepository.findAllByIdForUpdate(List.of(30L)))
                .thenReturn(List.of(pet));
        when(petRepository.findAllById(List.of(30L)))
                .thenReturn(List.of(pet));

        when(pricingService.calculate(any(PricingContext.class)))
                .thenReturn(new BookingPriceResponse(
                        new BigDecimal("1000.00"),
                        new BigDecimal("0.00"),
                        new BigDecimal("100.00"),
                        new BigDecimal("50.00"),
                        new BigDecimal("1050.00")
                ));

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> {
                    Booking booking = invocation.getArgument(0);
                    booking.setId(1L);
                    return booking;
                });
    }

    private Booking capturedBooking() {
        ArgumentCaptor<Booking> captor =
                ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        return captor.getValue();
    }

    private Booking existingBooking(BookingStatus status) {
        return Booking.builder()
                .id(1L)
                .user(user)
                .room(room)
                .status(status)
                .paymentStatus(PaymentStatus.UNPAID)
                .checkInDate(LocalDate.of(2026, 10, 10))
                .checkOutDate(LocalDate.of(2026, 10, 12))
                .totalPrice(new BigDecimal("1000.00"))
                .build();
    }

    private void prepareUpdate(Booking booking) {
        when(bookingRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
    }

    private void verifyNoSave() {
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    private void assertCheckInOn(LocalDate today, boolean allowed) {
        Booking booking = existingBooking(BookingStatus.CONFIRMED);
        prepareUpdate(booking);
        ZoneId zone = ZoneId.of("Asia/Bangkok");

        try (MockedStatic<LocalDate> dates =
                     mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {

            dates.when(() -> LocalDate.now(zone)).thenReturn(today);

            if (allowed) {
                BookingResponse response = bookingService.checkIn(1L);
                assertEquals(BookingStatus.CHECKED_IN, response.getStatus());
                assertEquals(BookingStatus.CHECKED_IN, booking.getStatus());
                verify(bookingRepository).save(booking);
            } else {
                assertThrows(
                        IllegalStateException.class,
                        () -> bookingService.checkIn(1L)
                );
                assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
                verifyNoSave();
            }
        }
    }

    @Test
    void duplicateServiceRecipientShouldReject() {
        prepareCreate();
        CreateBookingRequest request = validRequest();
        request.setServicePetIds(Map.of(40L, List.of(30L, 30L)));
        assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(request));
        verifyNoInteractions(extraServiceRepository);
        verifyNoSave();
    }

    @Test
    void previewShouldNotSaveLockRoomOrPublishEvent() {
        prepareCreate();
        when(roomRepository.findById(20L)).thenReturn(Optional.of(room));
        assertNotNull(bookingService.previewPrice(validRequest()));
        verify(roomRepository).findById(20L);
        verify(roomRepository, never()).findByIdForUpdate(anyLong());
        verify(petRepository, never()).findAllByIdForUpdate(anyCollection());
        verifyNoInteractions(
                bookingRepository,
                bookingPetRepository,
                eventPublisher,
                availabilityService
        );
    }

    @Test
    void previewAndCreateShouldUseSamePricingInputsAndRecipient() {
        prepareCreate();
        when(roomRepository.findById(20L)).thenReturn(Optional.of(room));
        ExtraService extra = new ExtraService();
        extra.setId(40L);
        extra.setActive(true);
        extra.setPrice(new BigDecimal("300.00"));
        when(extraServiceRepository.findById(40L)).thenReturn(Optional.of(extra));

        CreateBookingRequest request = validRequest();
        request.setServicePetIds(Map.of(40L, List.of(30L)));
        bookingService.previewPrice(request);
        bookingService.createBooking(request);

        ArgumentCaptor<PricingContext> captor = ArgumentCaptor.forClass(PricingContext.class);
        verify(pricingService, times(2)).calculate(captor.capture());
        PricingContext preview = captor.getAllValues().get(0);
        PricingContext created = captor.getAllValues().get(1);

        assertSame(preview.getRoom(), created.getRoom());
        assertEquals(preview.getPetCount(), created.getPetCount());
        assertEquals(preview.getNights(), created.getNights());
        assertEquals(preview.getCheckIn(), created.getCheckIn());
        assertEquals(preview.getCheckOut(), created.getCheckOut());
        assertEquals(preview.getExtraServices().get(0).getTotalPrice(),
                created.getExtraServices().get(0).getTotalPrice());
        assertSame(pet, preview.getExtraServices().get(0).getBookingPet().getPet());
        assertNull(preview.getExtraServices().get(0).getBookingPet().getId());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void twoRecipientsShouldCostTwoUnitsWithoutMultiplyingByNights() {
        prepareCreate();
        Pet secondPet = new Pet();
        secondPet.setId(31L);
        secondPet.setOwner(user);
        secondPet.setActive(true);
        CreateBookingRequest request = validRequest();
        request.setPetIds(List.of(30L, 31L));
        request.setServicePetIds(Map.of(40L, List.of(30L, 31L)));
        when(petRepository.findAllByIdForUpdate(request.getPetIds()))
                .thenReturn(List.of(pet, secondPet));
        ExtraService extra = new ExtraService();
        extra.setId(40L);
        extra.setActive(true);
        extra.setPrice(new BigDecimal("300.00"));
        when(extraServiceRepository.findById(40L)).thenReturn(Optional.of(extra));

        bookingService.createBooking(request);
        Booking saved = capturedBooking();
        assertEquals(2, saved.getExtraServices().size());
        for (BookingExtraService item : saved.getExtraServices()) {
            assertSame(saved, item.getBookingPet().getBooking());
            assertTrue(saved.getBookingPets().contains(item.getBookingPet()));
            assertEquals(Integer.valueOf(1), item.getQuantity());
        }

        ArgumentCaptor<PricingContext> captor = ArgumentCaptor.forClass(PricingContext.class);
        verify(pricingService).calculate(captor.capture());
        BigDecimal extraTotal = new com.example.petshotel.pricing.ExtraServicePricingStrategy()
                .calculate(captor.getValue(), BigDecimal.ZERO);
        assertEquals(0, extraTotal.compareTo(new BigDecimal("600.00")));
    }

    @Test
    void mapperShouldKeepHistoricalServiceWithoutRecipientReadable() {
        Booking booking = existingBooking(BookingStatus.CONFIRMED);
        ExtraService extra = new ExtraService();
        extra.setName("Historical bath");
        BookingExtraService historical = new BookingExtraService();
        historical.setExtraService(extra);
        historical.setQuantity(2);
        historical.setUnitPrice(new BigDecimal("300.00"));
        historical.setTotalPrice(new BigDecimal("600.00"));
        booking.addExtraService(historical);
        BookingResponse response = new BookingMapper().toResponse(booking);
        assertNull(response.getExtraServices().get(0).petId());
        assertNull(response.getExtraServices().get(0).petName());
        assertEquals(new BigDecimal("600.00"),
                response.getExtraServices().get(0).totalPrice());
    }

        @Test
        void nullServiceRecipientListShouldBeIgnored() {
        prepareCreate();

        CreateBookingRequest request = validRequest();

        Map<Long, List<Long>> selections =
                new HashMap<>();

        selections.put(40L, null);
        request.setServicePetIds(selections);

        bookingService.createBooking(request);

        Booking saved = capturedBooking();

        assertTrue(saved.getExtraServices().isEmpty());
        verifyNoInteractions(extraServiceRepository);
        }

}
