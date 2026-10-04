package com.example.petshotel.exception;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.server.ResponseStatusException;
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest("GET", "/api/test");
    }

    @Test
    void notFoundShouldReturn404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(new ResourceNotFoundException("Room", 99L), request);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Room not found: 99", response.getBody().message());
        assertEquals("/api/test", response.getBody().path());
    }

    @Test
    void roomNotAvailableShouldReturn409() {
        RoomNotAvailableException ex =
                new RoomNotAvailableException(1L, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3));

        ResponseEntity<ErrorResponse> response = handler.handleConflict(ex, request);

        assertEquals(409, response.getStatusCode().value());
    }

    @Test
    void invalidBookingStateShouldReturn409() {
        ResponseEntity<ErrorResponse> response =
                handler.handleConflict(new InvalidBookingStateException("Pending booking cannot check in"), request);

        assertEquals(409, response.getStatusCode().value());
        assertEquals("Pending booking cannot check in", response.getBody().message());
    }

    @Test
    void illegalArgumentShouldReturn400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBadRequest(new IllegalArgumentException("Bad input"), request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Bad input", response.getBody().message());
    }

    @Test
    void validationErrorShouldReturn400WithFieldErrors() {
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(new Object(), "request");
        result.addError(new FieldError("request", "petCount", "must be greater than or equal to 1"));

        ResponseEntity<ErrorResponse> response = handler.handleValidation(new BindException(result), request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("must be greater than or equal to 1", response.getBody().fieldErrors().get("petCount"));
    }

    @Test
    void unexpectedErrorShouldReturn500WithoutLeakingDetails() {
        ResponseEntity<ErrorResponse> response =
                handler.handleUnexpected(new RuntimeException("database password wrong"), request);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Unexpected server error", response.getBody().message());
    }

    @Test
    void duplicateResourceShouldReturn409() {
        ResponseEntity<ErrorResponse> response =
                handler.handleConflict(new DuplicateResourceException("อีเมลนี้ถูกใช้สมัครแล้ว"), request);

        assertEquals(409, response.getStatusCode().value());
    }

    @Test
    void accessDeniedShouldReturn403() {
        ResponseEntity<ErrorResponse> response =
                handler.handleForbidden(new AccessDeniedException("not owner"), request);

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void missingRequestParameterShouldReturn400() {
        ResponseEntity<ErrorResponse> response = handler.handleMalformedRequest(
                new MissingServletRequestParameterException("petCount", "Integer"), request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("/api/test", response.getBody().path());
    }

    @Test
    void responseStatusExceptionShouldKeepStatusAndReason() {
        ResponseEntity<ErrorResponse> response = handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.FORBIDDEN, "ไม่มีสิทธิ์ดูการจองนี้"), request);

        assertEquals(403, response.getStatusCode().value());
        assertEquals("ไม่มีสิทธิ์ดูการจองนี้", response.getBody().message());
    }

    @Test
    void responseStatusExceptionWithoutReasonShouldUseStatusPhrase() {
        ResponseEntity<ErrorResponse> response = handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.NOT_FOUND), request);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Not Found", response.getBody().message());
    }
}