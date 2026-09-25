package com.example.petshotel.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.domain.entity.BookingPet;
import com.example.petshotel.domain.entity.DailyCareReport;
import com.example.petshotel.domain.entity.Pet;
import com.example.petshotel.domain.entity.User;
import com.example.petshotel.domain.enums.BookingStatus;
import com.example.petshotel.domain.enums.UserRole;
import com.example.petshotel.dto.request.CreateDailyCareReportRequest;
import com.example.petshotel.dto.response.DailyCareReportResponse;
import com.example.petshotel.mapper.DailyCareReportMapper;
import com.example.petshotel.repository.BookingPetRepository;
import com.example.petshotel.repository.DailyCareReportRepository;
import com.example.petshotel.repository.UserRepository;
import com.example.petshotel.service.impl.DailyCareReportServiceImpl;
import java.time.LocalDateTime;
import com.example.petshotel.dto.request.UpdateDailyCareReportRequest;

public class DailyCareReportServiceTest {
    private DailyCareReportRepository reportRepository;
    private BookingPetRepository bookingPetRepository;
    private UserRepository userRepository;
    private DailyCareReportServiceImpl reportService;

    private User staff;
    private BookingPet bookingPet;
    private CreateDailyCareReportRequest request;

    @BeforeEach
    void setUp() {
        reportRepository = mock(DailyCareReportRepository.class);
        bookingPetRepository = mock(BookingPetRepository.class);
        userRepository = mock(UserRepository.class);

        reportService = new DailyCareReportServiceImpl(
                reportRepository,
                bookingPetRepository,
                userRepository,
                new DailyCareReportMapper()
        );

        staff = new User();
        staff.setId(5L);
        staff.setRole(UserRole.STAFF);

        Booking booking = new Booking();
        booking.setId(10L);
        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.setCheckInDate(LocalDate.of(2026, 9, 23));
        booking.setCheckOutDate(LocalDate.of(2026, 9, 25));

        Pet pet = new Pet();
        pet.setId(7L);
        pet.setName("โมจิ");

        bookingPet = new BookingPet();
        bookingPet.setId(21L);
        bookingPet.setBooking(booking);
        bookingPet.setPet(pet);

        request = new CreateDailyCareReportRequest(
                LocalDate.of(2026, 9, 24),
                "กินหมด",
                "กินครึ่งหนึ่ง",
                20,
                "แปรงขน",
                "ร่าเริง",
                "ปกติ",
                "ชอบเล่นลูกบอล"
        );
    }

    @Test
    void createReportShouldSaveWhenDataIsValid() {
        when(userRepository.findById(5L))
                .thenReturn(Optional.of(staff));

        when(bookingPetRepository.findById(21L))
                .thenReturn(Optional.of(bookingPet));

        when(reportRepository.existsByBookingPet_IdAndReportDate(
                21L, request.reportDate()))
                .thenReturn(false);

        when(reportRepository.save(any(DailyCareReport.class)))
                .thenAnswer(invocation -> {
                    DailyCareReport report = invocation.getArgument(0);
                    report.setId(100L);
                    return report;
                });

        DailyCareReportResponse response =
                reportService.createReport(21L, 5L, request);

        assertEquals(Long.valueOf(100L), response.id());
        assertEquals(Long.valueOf(21L), response.bookingPetId());
        assertEquals(Long.valueOf(10L), response.bookingId());
        assertEquals(Long.valueOf(7L), response.petId());
        assertEquals("โมจิ", response.petName());
        assertEquals(Long.valueOf(5L), response.recordedById());
        assertEquals(request.reportDate(), response.reportDate());
        assertEquals("กินหมด", response.feedingMorning());
        assertEquals(Integer.valueOf(20), response.walkingMinutes());

        verify(reportRepository).save(any(DailyCareReport.class));
    }

    @Test
    void createReportShouldRejectDuplicatePetAndDate() {
        when(userRepository.findById(5L))
                .thenReturn(Optional.of(staff));

        when(bookingPetRepository.findById(21L))
                .thenReturn(Optional.of(bookingPet));

        when(reportRepository.existsByBookingPet_IdAndReportDate(
                21L, request.reportDate()))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> reportService.createReport(21L, 5L, request)
        );

        verify(reportRepository, never())
                .save(any(DailyCareReport.class));
    }

    @Test
    void createReportShouldRejectCustomer() {
    staff.setRole(UserRole.CUSTOMER);

    when(userRepository.findById(5L))
            .thenReturn(Optional.of(staff));

    assertThrows(
            IllegalArgumentException.class,
            () -> reportService.createReport(21L, 5L, request)
    );

    verify(reportRepository, never())
            .save(any(DailyCareReport.class));
    }

    @Test
    void createReportShouldRejectDateBeforeCheckIn() {
    CreateDailyCareReportRequest invalidRequest =
            new CreateDailyCareReportRequest(
                    LocalDate.of(2026, 9, 22),
                    "กินหมด",
                    "กินหมด",
                    20,
                    "แปรงขน",
                    "ร่าเริง",
                    "ปกติ",
                    null
            );

    when(userRepository.findById(5L))
            .thenReturn(Optional.of(staff));

    when(bookingPetRepository.findById(21L))
            .thenReturn(Optional.of(bookingPet));

    assertThrows(
            IllegalArgumentException.class,
            () -> reportService.createReport(21L, 5L, invalidRequest)
    );

    verify(reportRepository, never())
            .save(any(DailyCareReport.class));
    }

    @Test
    void createReportShouldRejectDateAfterCheckOut() {
    CreateDailyCareReportRequest invalidRequest =
            new CreateDailyCareReportRequest(
                    LocalDate.of(2026, 9, 26),
                    "กินหมด",
                    "กินหมด",
                    20,
                    "แปรงขน",
                    "ร่าเริง",
                    "ปกติ",
                    null
            );

    when(userRepository.findById(5L))
            .thenReturn(Optional.of(staff));

    when(bookingPetRepository.findById(21L))
            .thenReturn(Optional.of(bookingPet));

    assertThrows(
            IllegalArgumentException.class,
            () -> reportService.createReport(21L, 5L, invalidRequest)
    );

    verify(reportRepository, never())
            .save(any(DailyCareReport.class));
    }

    private DailyCareReport createExistingReport(User owner) {
    bookingPet.getBooking().setUser(owner);

    DailyCareReport report = new DailyCareReport();
    report.setId(100L);
    report.setBookingPet(bookingPet);
    report.setRecordedBy(staff);
    report.setReportDate(request.reportDate());
    report.setFeedingMorning("กินหมด");

    return report;
    }

    @Test
    void getReportByIdShouldAllowBookingOwner() {
    User owner = new User();
    owner.setId(8L);
    owner.setRole(UserRole.CUSTOMER);

    DailyCareReport report = createExistingReport(owner);

    when(userRepository.findById(8L))
            .thenReturn(Optional.of(owner));

    when(reportRepository.findById(100L))
            .thenReturn(Optional.of(report));

    DailyCareReportResponse response =
            reportService.getReportById(100L, 8L);

    assertEquals(Long.valueOf(100L), response.id());
    assertEquals(Long.valueOf(10L), response.bookingId());
    assertEquals("โมจิ", response.petName());
    assertEquals("กินหมด", response.feedingMorning());
    }

    @Test
    void getReportByIdShouldRejectOtherCustomer() {
    User owner = new User();
    owner.setId(8L);
    owner.setRole(UserRole.CUSTOMER);

    User otherCustomer = new User();
    otherCustomer.setId(9L);
    otherCustomer.setRole(UserRole.CUSTOMER);

    DailyCareReport report = createExistingReport(owner);

    when(userRepository.findById(9L))
            .thenReturn(Optional.of(otherCustomer));

    when(reportRepository.findById(100L))
            .thenReturn(Optional.of(report));

    assertThrows(
            IllegalArgumentException.class,
            () -> reportService.getReportById(100L, 9L)
    );
    }

    @Test
    void getReportByIdShouldAllowStaff() {
    User owner = new User();
    owner.setId(8L);
    owner.setRole(UserRole.CUSTOMER);

    DailyCareReport report = createExistingReport(owner);

    when(userRepository.findById(5L))
            .thenReturn(Optional.of(staff));

    when(reportRepository.findById(100L))
            .thenReturn(Optional.of(report));

    DailyCareReportResponse response =
            reportService.getReportById(100L, 5L);

    assertEquals(Long.valueOf(100L), response.id());
    assertEquals("โมจิ", response.petName());
    }

    private UpdateDailyCareReportRequest createUpdateRequest(Long bookingPetId, LocalDate reportDate) {

    return new UpdateDailyCareReportRequest(
            bookingPetId,
            reportDate,
            "กินหมดและเติมอาหาร",
            "กินหมด",
            30,
            "แปรงขน",
            "ร่าเริง",
            "ปกติ",
            "แก้ไขรายละเอียดการดูแล"
    );
    }

    @Test
    void updateReportShouldAllowSamePetAndDate() {
    User owner = new User();
    owner.setId(8L);

    DailyCareReport report = createExistingReport(owner);

    User originalRecorder = new User();
    originalRecorder.setId(6L);
    originalRecorder.setRole(UserRole.STAFF);

    LocalDateTime originalCreatedAt =
            LocalDateTime.of(2026, 9, 24, 9, 0);

    report.setRecordedBy(originalRecorder);
    report.setCreatedAt(originalCreatedAt);

    UpdateDailyCareReportRequest updateRequest =
            createUpdateRequest(21L, report.getReportDate());

    when(userRepository.findById(5L))
            .thenReturn(Optional.of(staff));

    when(reportRepository.findById(100L))
            .thenReturn(Optional.of(report));

    when(bookingPetRepository.findById(21L))
            .thenReturn(Optional.of(bookingPet));

    when(reportRepository.existsByBookingPet_IdAndReportDateAndIdNot(
            21L, updateRequest.reportDate(), 100L))
            .thenReturn(false);

    when(reportRepository.save(any(DailyCareReport.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    DailyCareReportResponse response =
            reportService.updateReport(100L, 5L, updateRequest);

    assertEquals(Long.valueOf(100L), response.id());
    assertEquals(Long.valueOf(21L), response.bookingPetId());
    assertEquals(updateRequest.reportDate(), response.reportDate());
    assertEquals("กินหมดและเติมอาหาร", response.feedingMorning());
    assertEquals(Integer.valueOf(30), response.walkingMinutes());

    assertEquals(Long.valueOf(6L), response.recordedById());
    assertEquals(originalCreatedAt, response.createdAt());

    verify(reportRepository)
            .existsByBookingPet_IdAndReportDateAndIdNot(
                    21L, updateRequest.reportDate(), 100L);

    verify(reportRepository).save(report);
    }

    @Test
    void updateReportShouldRejectPetFromAnotherBooking() {
    User owner = new User();
    owner.setId(8L);

    DailyCareReport report = createExistingReport(owner);

    Booking anotherBooking = new Booking();
    anotherBooking.setId(99L);

    BookingPet anotherBookingPet = new BookingPet();
    anotherBookingPet.setId(30L);
    anotherBookingPet.setBooking(anotherBooking);

    UpdateDailyCareReportRequest updateRequest =
            createUpdateRequest(30L, request.reportDate());

    when(userRepository.findById(5L))
            .thenReturn(Optional.of(staff));

    when(reportRepository.findById(100L))
            .thenReturn(Optional.of(report));

    when(bookingPetRepository.findById(30L))
            .thenReturn(Optional.of(anotherBookingPet));

    assertThrows(
            IllegalArgumentException.class,
            () -> reportService.updateReport(100L, 5L, updateRequest)
    );

    assertSame(bookingPet, report.getBookingPet());

    verify(reportRepository, never())
            .save(any(DailyCareReport.class));
    }

    @Test
    void updateReportShouldRejectDuplicatePetAndDate() {
    User owner = new User();
    owner.setId(8L);

    DailyCareReport report = createExistingReport(owner);
    LocalDate originalDate = report.getReportDate();

    UpdateDailyCareReportRequest updateRequest =
            createUpdateRequest(
                    21L,
                    LocalDate.of(2026, 9, 25)
            );

    when(userRepository.findById(5L))
            .thenReturn(Optional.of(staff));

    when(reportRepository.findById(100L))
            .thenReturn(Optional.of(report));

    when(bookingPetRepository.findById(21L))
            .thenReturn(Optional.of(bookingPet));

    when(reportRepository.existsByBookingPet_IdAndReportDateAndIdNot(
            21L, updateRequest.reportDate(), 100L))
            .thenReturn(true);

    assertThrows(
            IllegalStateException.class,
            () -> reportService.updateReport(100L, 5L, updateRequest)
    );

    assertEquals(originalDate, report.getReportDate());

    verify(reportRepository, never())
            .save(any(DailyCareReport.class));
    }

    @Test
    void updateReportShouldAllowAnotherPetInSameBooking() {
    User owner = new User();
    owner.setId(8L);

    DailyCareReport report = createExistingReport(owner);

    Pet anotherPet = new Pet();
    anotherPet.setId(9L);
    anotherPet.setName("มะลิ");

    BookingPet anotherBookingPet = new BookingPet();
    anotherBookingPet.setId(22L);
    anotherBookingPet.setBooking(bookingPet.getBooking());
    anotherBookingPet.setPet(anotherPet);

    UpdateDailyCareReportRequest updateRequest =
            createUpdateRequest(22L, request.reportDate());

    when(userRepository.findById(5L))
            .thenReturn(Optional.of(staff));

    when(reportRepository.findById(100L))
            .thenReturn(Optional.of(report));

    when(bookingPetRepository.findById(22L))
            .thenReturn(Optional.of(anotherBookingPet));

    when(reportRepository.existsByBookingPet_IdAndReportDateAndIdNot(
            22L, updateRequest.reportDate(), 100L))
            .thenReturn(false);

    when(reportRepository.save(any(DailyCareReport.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    DailyCareReportResponse response =
            reportService.updateReport(100L, 5L, updateRequest);

    assertEquals(Long.valueOf(100L), response.id());
    assertEquals(Long.valueOf(10L), response.bookingId());
    assertEquals(Long.valueOf(22L), response.bookingPetId());
    assertEquals(Long.valueOf(9L), response.petId());
    assertEquals("มะลิ", response.petName());

    assertSame(anotherBookingPet, report.getBookingPet());
    verify(reportRepository).save(report);
    }

    @Test
    void updateReportShouldRejectDateOutsideStay() {
    User owner = new User();
    owner.setId(8L);

    DailyCareReport report = createExistingReport(owner);
    LocalDate originalDate = report.getReportDate();

    UpdateDailyCareReportRequest updateRequest =
            createUpdateRequest(
                    21L,
                    LocalDate.of(2026, 9, 26)
            );

    when(userRepository.findById(5L))
            .thenReturn(Optional.of(staff));

    when(reportRepository.findById(100L))
            .thenReturn(Optional.of(report));

    when(bookingPetRepository.findById(21L))
            .thenReturn(Optional.of(bookingPet));

    assertThrows(
            IllegalArgumentException.class,
            () -> reportService.updateReport(100L, 5L, updateRequest)
    );

    assertEquals(originalDate, report.getReportDate());

    verify(reportRepository, never())
            .save(any(DailyCareReport.class));
    }

    @Test
    void updateReportShouldRejectCustomer() {
    User customer = new User();
    customer.setId(8L);
    customer.setRole(UserRole.CUSTOMER);

    UpdateDailyCareReportRequest updateRequest =
            createUpdateRequest(21L, request.reportDate());

    when(userRepository.findById(8L))
            .thenReturn(Optional.of(customer));

    assertThrows(
            IllegalArgumentException.class,
            () -> reportService.updateReport(100L, 8L, updateRequest)
    );

    verify(reportRepository, never())
            .save(any(DailyCareReport.class));
    }
}
