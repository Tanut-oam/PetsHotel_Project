package com.example.petshotel.exception;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class PetNotAvailableException
        extends IllegalStateException {

    private static final DateTimeFormatter THAI_DATE =
            DateTimeFormatter.ofPattern(
                    "d MMMM uuuu",
                    Locale.forLanguageTag("th-TH")
            );

    public PetNotAvailableException(
            List<String> petNames,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        super(buildMessage(
                petNames,
                checkIn,
                checkOut
        ));
    }

    private static String buildMessage(
            List<String> petNames,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        String names = petNames.stream()
                .map(name -> "“" + name + "”")
                .reduce((first, next) ->
                        first + ", " + next)
                .orElse("ที่เลือก");

        return "สัตว์เลี้ยง "
                + names
                + " มีการจองระหว่างวันที่ "
                + checkIn.format(THAI_DATE)
                + " ถึง "
                + checkOut.format(THAI_DATE)
                + " อยู่แล้ว กรุณาเปลี่ยนช่วงวันเข้าพัก";
    }
}