package com.example.petshotel.controller.api;

import com.example.petshotel.config.SecurityConfig;
import com.example.petshotel.domain.entity.User;
import com.example.petshotel.domain.enums.BookingStatus;
import com.example.petshotel.domain.enums.UserRole;
import com.example.petshotel.dto.request.CreateBookingRequest;
import com.example.petshotel.dto.response.BookingResponse;
import com.example.petshotel.exception.GlobalExceptionHandler;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.repository.UserRepository;
import com.example.petshotel.service.BookingService;
import com.example.petshotel.service.PaymentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitConfig(BookingRestControllerTest.TestConfig.class)
@WebAppConfiguration
class BookingRestControllerTest {

    private static final String EMAIL = "customer@example.com";

    private static final String VALID_JSON = """
            {
                "roomId": 20,
                "petIds": [30],
                "checkInDate": "2026-10-10",
                "checkOutDate": "2026-10-12"
            }
            """;

    @Configuration
    @EnableWebMvc
    @Import({
            SecurityConfig.class,
            BookingRestController.class,
            PaymentRestController.class,
            GlobalExceptionHandler.class
    })
    static class TestConfig {

        @Bean
        BookingService bookingService() {
            return mock(BookingService.class);
        }

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        PaymentService paymentService() {
            return mock(PaymentService.class);
        }
    }

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentService paymentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Spring reuses this context, so reset mocks between tests.
        reset(bookingService, userRepository, paymentService);

        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        account(UserRole.CUSTOMER);
    }

    @Test
    void anonymousReadShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/bookings/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(bookingService);
    }

    @Test
    void postWithoutCsrfShouldReturn403() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .with(user(EMAIL).roles("CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookingService);
    }

    @Test
    void createShouldUseLoggedInUserWithoutUserIdInBody() throws Exception {
        when(bookingService.createBooking(any(CreateBookingRequest.class)))
                .thenReturn(response(10L, BookingStatus.PENDING));

        mockMvc.perform(post("/api/bookings")
                        .with(user(EMAIL).roles("CUSTOMER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.status").value("PENDING"));

        ArgumentCaptor<CreateBookingRequest> captor =
                ArgumentCaptor.forClass(CreateBookingRequest.class);
        verify(bookingService).createBooking(captor.capture());

        assertEquals(10L, captor.getValue().getUserId());
        assertEquals(20L, captor.getValue().getRoomId());
        assertEquals(List.of(30L), captor.getValue().getPetIds());
        assertEquals(
                LocalDate.of(2026, 10, 10),
                captor.getValue().getCheckInDate()
        );
    }

    @Test
    void forgedUserIdMustNeverReachService() throws Exception {
        when(bookingService.createBooking(any(CreateBookingRequest.class)))
                .thenReturn(response(10L, BookingStatus.PENDING));

        String forgedJson = VALID_JSON.replace(
                "\"roomId\": 20",
                "\"userId\": 999, \"roomId\": 20"
        );

        int statusCode = mockMvc.perform(post("/api/bookings")
                        .with(user(EMAIL).roles("CUSTOMER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forgedJson))
                .andReturn()
                .getResponse()
                .getStatus();

        // Depending on Jackson configuration, unknown userId is either
        // rejected or ignored. It must never become the booking owner.
        assertTrue(
                statusCode == 400 || statusCode == 201,
                "Expected rejection or safe creation, got " + statusCode
        );

        if (statusCode == 400) {
            verifyNoInteractions(bookingService);
        } else {
            ArgumentCaptor<CreateBookingRequest> captor =
                    ArgumentCaptor.forClass(CreateBookingRequest.class);
            verify(bookingService).createBooking(captor.capture());
            assertEquals(10L, captor.getValue().getUserId());
        }
    }

    @Test
    void invalidRequestShouldReturn400WithoutCallingService() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .with(user(EMAIL).roles("CUSTOMER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void nullPetIdShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .with(user(EMAIL).roles("CUSTOMER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON.replace("[30]", "[null]")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void customerCannotListAllBookings() throws Exception {
        mockMvc.perform(get("/api/bookings")
                        .with(user(EMAIL).roles("CUSTOMER")))
                .andExpect(status().isForbidden());

        // Proves that SecurityConfig blocks before the Controller runs.
        verifyNoInteractions(userRepository, bookingService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"STAFF", "ADMIN"})
    void staffAndAdminCanListAllBookings(String role) throws Exception {
        account(UserRole.valueOf(role));
        when(bookingService.getAllBookings())
                .thenReturn(List.of(response(99L, BookingStatus.CONFIRMED)));

        mockMvc.perform(get("/api/bookings")
                        .with(user(EMAIL).roles(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(99));

        verify(bookingService).getAllBookings();
    }

    @Test
    void ownerCanReadOwnBooking() throws Exception {
        when(bookingService.getBookingById(1L))
                .thenReturn(response(10L, BookingStatus.CONFIRMED));

        mockMvc.perform(get("/api/bookings/1")
                        .with(user(EMAIL).roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void customerCannotReadAnotherOwnersBooking() throws Exception {
        when(bookingService.getBookingById(1L))
                .thenReturn(response(99L, BookingStatus.CONFIRMED));

        mockMvc.perform(get("/api/bookings/1")
                        .with(user(EMAIL).roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"STAFF", "ADMIN"})
    void staffAndAdminCanReadAnotherOwnersBooking(String role) throws Exception {
        account(UserRole.valueOf(role));
        when(bookingService.getBookingById(1L))
                .thenReturn(response(99L, BookingStatus.CONFIRMED));

        mockMvc.perform(get("/api/bookings/1")
                        .with(user(EMAIL).roles(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(99));
    }

    @Test
    void missingBookingShouldReturn404() throws Exception {
        when(bookingService.getBookingById(99L))
                .thenThrow(new ResourceNotFoundException("Booking", 99L));

        mockMvc.perform(get("/api/bookings/99")
                        .with(user(EMAIL).roles("CUSTOMER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidBookingIdShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/bookings/abc")
                        .with(user(EMAIL).roles("CUSTOMER")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void ownerCanRequestCancellation() throws Exception {
        when(bookingService.getBookingById(1L))
                .thenReturn(response(10L, BookingStatus.CONFIRMED));
        when(bookingService.cancelBooking(1L))
                .thenReturn(response(10L, BookingStatus.CANCELLED));

        mockMvc.perform(post("/api/bookings/1/cancel")
                        .with(user(EMAIL).roles("CUSTOMER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(bookingService).cancelBooking(1L);
    }

    @Test
    void customerCannotCancelAnotherOwnersBooking() throws Exception {
        when(bookingService.getBookingById(1L))
                .thenReturn(response(99L, BookingStatus.CONFIRMED));

        mockMvc.perform(post("/api/bookings/1/cancel")
                        .with(user(EMAIL).roles("CUSTOMER"))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(bookingService, never()).cancelBooking(anyLong());
    }

    @ParameterizedTest
    @ValueSource(strings = {"STAFF", "ADMIN"})
    void staffAndAdminCanRequestCancellationForAnotherOwner(String role)
            throws Exception {

        account(UserRole.valueOf(role));
        when(bookingService.getBookingById(1L))
                .thenReturn(response(99L, BookingStatus.CONFIRMED));
        when(bookingService.cancelBooking(1L))
                .thenReturn(response(99L, BookingStatus.CANCELLED));

        mockMvc.perform(post("/api/bookings/1/cancel")
                        .with(user(EMAIL).roles(role))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(bookingService).cancelBooking(1L);
    }

    @Test
    void permittedOwnerStillReceives409WhenServiceRejectsCancellation()
            throws Exception {

        when(bookingService.getBookingById(1L))
                .thenReturn(response(10L, BookingStatus.CONFIRMED));
        when(bookingService.cancelBooking(1L))
                .thenThrow(new IllegalStateException(
                        "Paid bookings cannot be cancelled through this operation"
                ));

        mockMvc.perform(post("/api/bookings/1/cancel")
                        .with(user(EMAIL).roles("CUSTOMER"))
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "confirm", "check-in", "check-out", "payment/confirm"
    })
    void customerCannotPerformStaffOperations(String action) throws Exception {
        mockMvc.perform(post("/api/bookings/1/" + action)
                        .with(user(EMAIL).roles("CUSTOMER"))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userRepository, bookingService, paymentService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"STAFF", "ADMIN"})
    void staffAndAdminCanPerformStateTransitions(String role) throws Exception {
        account(UserRole.valueOf(role));

        when(bookingService.confirmBooking(1L))
                .thenReturn(response(99L, BookingStatus.CONFIRMED));
        when(bookingService.checkIn(1L))
                .thenReturn(response(99L, BookingStatus.CHECKED_IN));
        when(bookingService.checkOut(1L))
                .thenReturn(response(99L, BookingStatus.CHECKED_OUT));

        mockMvc.perform(post("/api/bookings/1/confirm")
                        .with(user(EMAIL).roles(role)).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        mockMvc.perform(post("/api/bookings/1/check-in")
                        .with(user(EMAIL).roles(role)).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_IN"));

        mockMvc.perform(post("/api/bookings/1/check-out")
                        .with(user(EMAIL).roles(role)).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_OUT"));

        verify(bookingService).confirmBooking(1L);
        verify(bookingService).checkIn(1L);
        verify(bookingService).checkOut(1L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"STAFF", "ADMIN"})
    void staffAndAdminCanReachPaymentService(String role) throws Exception {
        account(UserRole.valueOf(role));

        mockMvc.perform(post("/api/bookings/1/payment/confirm")
                        .with(user(EMAIL).roles(role))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(paymentService).confirmPayment(1L);
    }

    @Test
    void inactiveAccountCannotReadBooking() throws Exception {
        User inactive = account(UserRole.CUSTOMER);
        inactive.setActive(false);

        mockMvc.perform(get("/api/bookings/1")
                        .with(user(EMAIL).roles("CUSTOMER")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookingService);
    }

    private User account(UserRole role) {
        User account = new User();
        account.setId(10L);
        account.setEmail(EMAIL);
        account.setRole(role);
        account.setActive(true);

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(account));

        return account;
    }

    private BookingResponse response(Long ownerId, BookingStatus state) {
        return BookingResponse.builder()
                .id(1L)
                .userId(ownerId)
                .roomId(20L)
                .petIds(List.of(30L))
                .checkInDate(LocalDate.of(2026, 10, 10))
                .checkOutDate(LocalDate.of(2026, 10, 12))
                .status(state)
                .totalPrice(new BigDecimal("1000.00"))
                .build();
    }
}