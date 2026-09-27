package com.example.petshotel.exception;

public class InvalidBookingStateException extends IllegalStateException {

    public InvalidBookingStateException(String message) {
        super(message);
    }
}