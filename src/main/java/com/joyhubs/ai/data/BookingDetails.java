package com.joyhubs.ai.data;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BookingDetails(String bookingNumber,
                             String firstName,
                             String lastName,
                             LocalDate date,
                             BookingStatus bookingStatus,
                             String from,
                             String to,
                             String seatNumber,
                             String bookingClass)  {
}
