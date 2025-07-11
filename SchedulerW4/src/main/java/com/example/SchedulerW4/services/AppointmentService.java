package com.example.SchedulerW4.services;

import com.example.SchedulerW4.dtos.appointment_dtos.AppointmentRequestDto;
import com.example.SchedulerW4.dtos.appointment_dtos.AppointmentResponseDto;
import com.example.SchedulerW4.dtos.appointment_dtos.BookingResponseDto; // NEW IMPORT
import com.example.SchedulerW4.entities.User;

import java.util.List;

public interface AppointmentService {

    // MODIFIED: Return type changed to BookingResponseDto
    BookingResponseDto bookAppointment (AppointmentRequestDto dto);

    List<AppointmentResponseDto> getAllAppointments (Long userId);

    List<AppointmentResponseDto> getProviderAppointments (Long providerId);

    // MODIFIED: Return type changed to BookingResponseDto
    BookingResponseDto rescheduleAppointment (Long appointmentId, Long newSlotId, Long userId);

    void cancelAppointment (Long appointmentId, Long userId);

}