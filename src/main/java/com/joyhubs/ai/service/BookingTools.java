package com.joyhubs.ai.service;

import com.joyhubs.ai.data.BookingDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * MCP Tools
 *
 * @author OxNPC
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingTools {

    private final FlightBookingService flightBookingService;

    @Tool(description = "Get All Bookings")
    public List<BookingDetails> getAllBookings() {
        try {
            return flightBookingService.getBookings();
        } catch (Exception e) {
            log.warn("Query all booking error: {}", NestedExceptionUtils.getMostSpecificCause(e).getMessage());
            return Collections.EMPTY_LIST;
        }
    }

    @Tool(description = "Get booking details")
    public BookingDetails getBookingDetails(String bookingNumber,
                                            String firstName,
                                            String lastName) {
        try {
            return flightBookingService.getBookingDetails(bookingNumber, firstName, lastName);
        } catch (Exception e) {
            log.warn("Booking details: {}", NestedExceptionUtils.getMostSpecificCause(e).getMessage());
            return new BookingDetails(bookingNumber, firstName, lastName, null, null,
                    null, null, null, null);
        }
    }

    @Tool(description = "Change booking dates")
    public Boolean changeBooking(String bookingNumber,
                                 String firstName,
                                 String lastName,
                                 String newDate,
                                 String from,
                                 String to) {
        return flightBookingService.changeBooking(bookingNumber, firstName, lastName, newDate, from, to);
    }


    @Tool(description = "Cancel booking")
    public Boolean cancelBooking(String bookingNumber,
                                 String firstName,
                                 String lastName) {
        return flightBookingService.cancelBooking(bookingNumber, firstName, lastName);
    }


}
