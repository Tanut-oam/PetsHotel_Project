package com.example.petshotel.controller.web;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.example.petshotel.config.SecurityConfig;
import com.example.petshotel.domain.entity.User;
import com.example.petshotel.domain.enums.BookingStatus;
import com.example.petshotel.domain.enums.PaymentStatus;
import com.example.petshotel.domain.enums.UserRole;
import com.example.petshotel.dto.request.CreateBookingRequest;
import com.example.petshotel.dto.response.BookingResponse;
import com.example.petshotel.repository.*;
import com.example.petshotel.service.BookingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitConfig(BookingControllerTest.TestConfig.class)
@WebAppConfiguration
class BookingControllerTest {

    private static final String EMAIL = "owner@example.com";

    @Configuration
    @EnableWebMvc
    @Import({
            SecurityConfig.class,
            BookingController.class,
            AdminBookingController.class
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
        RoomRepository roomRepository() {
            return mock(RoomRepository.class);
        }

        @Bean
        PetRepository petRepository() {
            return mock(PetRepository.class);
        }

        @Bean
        ExtraServiceRepository extraServiceRepository() {
            return mock(ExtraServiceRepository.class);
        }

        @Bean
        PromotionRepository promotionRepository() {
            return mock(PromotionRepository.class);
        }

        @Bean
        LocalValidatorFactoryBean validator() {
            return new LocalValidatorFactoryBean();
        }

        @Bean
        InternalResourceViewResolver viewResolver() {
            // ตรวจชื่อ view และ model โดยไม่ render Thymeleaf ในชุดนี้
            return new InternalResourceViewResolver("/test-views/", ".html");
        }
    }

    @Autowired
    WebApplicationContext context;

    @Autowired
    BookingService bookingService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    PetRepository petRepository;

    @Autowired
    ExtraServiceRepository extraServiceRepository;

    @Autowired
    PromotionRepository promotionRepository;

    MockMvc mvc;
    User account;

    @BeforeEach
    void setUp() {
        reset(
                bookingService,
                userRepository,
                roomRepository,
                petRepository,
                extraServiceRepository,
                promotionRepository
        );

        account = new User();
        account.setId(7L);
        account.setActive(true);
        account.setRole(UserRole.CUSTOMER);

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(account));

        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void anonymousShouldBeRedirectedToLogin() throws Exception {
        mvc.perform(get("/bookings"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        verifyNoInteractions(bookingService);
    }

    @Test
    void inactiveAccountShouldBeRejected() throws Exception {
        account.setActive(false);

        mvc.perform(get("/bookings").with(user(EMAIL)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookingService);
    }

    @Test
    void listShouldLoadOnlyLoggedInUsersBookings() throws Exception {
        List<BookingResponse> bookings = List.of(response(7L));

        when(bookingService.getBookingsByUserId(7L))
                .thenReturn(bookings);

        mvc.perform(get("/bookings").with(user(EMAIL)))
                .andExpect(status().isOk())
                .andExpect(view().name("my-bookings"))
                .andExpect(model().attribute("bookings", bookings));

        verify(bookingService).getBookingsByUserId(7L);
        verify(bookingService, never()).getAllBookings();
    }

    @Test
    void formShouldLoadChoicesAndSelectedRoom() throws Exception {
        mvc.perform(get("/bookings/new")
                        .param("roomId", "20")
                        .with(user(EMAIL)))
                .andExpect(status().isOk())
                .andExpect(view().name("booking"))
                .andExpect(model().attributeExists(
                        "bookingForm", "rooms", "pets",
                        "extraServices", "promotions"
                ));

        verify(petRepository).findByOwner_IdAndActiveTrue(7L);
    }

    @Test
    void createShouldIgnoreForgedUserIdAndRemoveZeroServices()
            throws Exception {

        when(bookingService.createBooking(any()))
                .thenReturn(response(7L));

        mvc.perform(validCreate()
                        .param("userId", "999")
                        .param("extraServiceQuantities[40]", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/1"));

        ArgumentCaptor<CreateBookingRequest> captor =
                ArgumentCaptor.forClass(CreateBookingRequest.class);

        verify(bookingService).createBooking(captor.capture());

        assertEquals(7L, captor.getValue().getUserId());
        assertFalse(
                captor.getValue().getExtraServiceQuantities().containsKey(40L)
        );
    }

    @Test
    void invalidFormShouldReturnErrorsWithoutCreatingBooking()
            throws Exception {

        mvc.perform(post("/bookings")
                        .with(user(EMAIL))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("booking"))
                .andExpect(model().attributeHasErrors("bookingForm"));

        verifyNoInteractions(bookingService);
    }

    @Test
    void serviceRejectionShouldReturnFormWithChoices() throws Exception {
        when(bookingService.createBooking(any()))
                .thenThrow(new IllegalStateException("Room unavailable"));

        mvc.perform(validCreate())
                .andExpect(status().isOk())
                .andExpect(view().name("booking"))
                .andExpect(model().attributeHasErrors("bookingForm"))
                .andExpect(model().attributeExists("rooms", "pets"));
    }

    @Test
    void createWithoutCsrfShouldBeRejected() throws Exception {
        mvc.perform(post("/bookings").with(user(EMAIL)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookingService);
    }

    @Test
    void ownerCanReadBooking() throws Exception {
        BookingResponse booking = response(7L);
        when(bookingService.getBookingById(1L)).thenReturn(booking);

        mvc.perform(get("/bookings/1").with(user(EMAIL)))
                .andExpect(status().isOk())
                .andExpect(view().name("booking-detail"))
                .andExpect(model().attribute("booking", booking));
    }

    @Test
    void anotherOwnerCannotReadBooking() throws Exception {
        when(bookingService.getBookingById(1L))
                .thenReturn(response(99L));

        mvc.perform(get("/bookings/1").with(user(EMAIL)))
                .andExpect(status().isNotFound());
    }

    @Test
    void anotherOwnerCannotCancelBooking() throws Exception {
        when(bookingService.getBookingById(1L))
                .thenReturn(response(99L));

        mvc.perform(post("/bookings/1/cancel")
                        .with(user(EMAIL))
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(bookingService, never()).cancelBooking(anyLong());
    }

    @Test
    void ownerCanCancelBooking() throws Exception {
        when(bookingService.getBookingById(1L))
                .thenReturn(response(7L));

        mvc.perform(post("/bookings/1/cancel")
                        .with(user(EMAIL))
                        .with(csrf()))
                .andExpect(redirectedUrl("/bookings/1"))
                .andExpect(flash().attributeExists("message"));

        verify(bookingService).cancelBooking(1L);
    }

    @Test
    void cancellationRejectionShouldShowError() throws Exception {
        when(bookingService.getBookingById(1L))
                .thenReturn(response(7L));

        when(bookingService.cancelBooking(1L))
                .thenThrow(new IllegalStateException("Paid booking"));

        mvc.perform(post("/bookings/1/cancel")
                        .with(user(EMAIL))
                        .with(csrf()))
                .andExpect(redirectedUrl("/bookings/1"))
                .andExpect(flash().attribute("error", "Paid booking"));
    }

    @Test
    void customerCannotOpenStaffPage() throws Exception {
        mvc.perform(get("/admin/bookings")
                        .with(user(EMAIL).roles("CUSTOMER")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookingService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"confirm", "check-in", "check-out", "cancel"})
    void customerCannotPerformStaffAction(String action) throws Exception {
        mvc.perform(post("/admin/bookings/1/" + action)
                        .with(user(EMAIL).roles("CUSTOMER"))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookingService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"STAFF", "ADMIN"})
    void staffAndAdminCanOpenManagementPage(String role) throws Exception {
        account.setRole(UserRole.valueOf(role));
        when(bookingService.getAllBookings()).thenReturn(List.of());

        mvc.perform(get("/admin/bookings")
                        .with(user(EMAIL).roles(role)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/bookings"));

        verify(bookingService).getAllBookings();
    }

    @ParameterizedTest
    @ValueSource(strings = {"confirm", "check-in", "check-out", "cancel"})
    void staffActionShouldCallCorrespondingService(String action)
            throws Exception {

        account.setRole(UserRole.STAFF);

        mvc.perform(post("/admin/bookings/1/" + action)
                        .with(user(EMAIL).roles("STAFF"))
                        .with(csrf()))
                .andExpect(redirectedUrl("/admin/bookings"))
                .andExpect(flash().attributeExists("message"));

        switch (action) {
            case "confirm" -> verify(bookingService).confirmBooking(1L);
            case "check-in" -> verify(bookingService).checkIn(1L);
            case "check-out" -> verify(bookingService).checkOut(1L);
            case "cancel" -> verify(bookingService).cancelBooking(1L);
        }

        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void staffActionWithoutCsrfShouldBeRejected() throws Exception {
        account.setRole(UserRole.STAFF);

        mvc.perform(post("/admin/bookings/1/confirm")
                        .with(user(EMAIL).roles("STAFF")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookingService);
    }

    @Test
    void unknownStaffActionShouldBeRejected() throws Exception {
        account.setRole(UserRole.STAFF);

        mvc.perform(post("/admin/bookings/1/unknown")
                        .with(user(EMAIL).roles("STAFF"))
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verifyNoInteractions(bookingService);
    }

    private MockHttpServletRequestBuilder validCreate() {
        return post("/bookings")
                .with(user(EMAIL))
                .with(csrf())
                .param("roomId", "20")
                .param("petIds", "30")
                .param("checkInDate", LocalDate.now().plusDays(1).toString())
                .param("checkOutDate", LocalDate.now().plusDays(3).toString());
    }

    private BookingResponse response(Long ownerId) {
        return BookingResponse.builder()
                .id(1L)
                .userId(ownerId)
                .roomId(20L)
                .petIds(List.of(30L))
                .status(BookingStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .build();
    }
}