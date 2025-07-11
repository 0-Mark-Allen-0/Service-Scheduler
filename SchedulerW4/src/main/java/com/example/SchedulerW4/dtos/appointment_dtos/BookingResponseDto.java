// src/main/java/com/example/SchedulerW4/dtos/appointment_dtos/BookingResponseDto.java
package com.example.SchedulerW4.dtos.appointment_dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingResponseDto {
    public enum BookingStatus {
        BOOKED,
        QUEUED,
        ALREADY_QUEUED, // Added for clarity if user tries to queue twice
        FAILED
    }

    private BookingStatus status;
    private String message;
    private AppointmentResponseDto appointment; // Will be present only if status is BOOKED
    private Long queuedSlotId; // Will be present if status is QUEUED or ALREADY_QUEUED
}