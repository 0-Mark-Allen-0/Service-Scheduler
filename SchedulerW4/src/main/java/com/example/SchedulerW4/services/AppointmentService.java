package com.example.SchedulerW4.services;

import com.example.SchedulerW4.dtos.appointment_dtos.AppointmentRequestDto;
import com.example.SchedulerW4.dtos.appointment_dtos.AppointmentResponseDto;
import com.example.SchedulerW4.entities.User;

import java.util.List;

public interface AppointmentService {

    AppointmentResponseDto bookAppointment (AppointmentRequestDto dto);

    List<AppointmentResponseDto> getAllAppointments (Long userId);

    List<AppointmentResponseDto> getProviderAppointments (Long providerId);

    AppointmentResponseDto rescheduleAppointment (Long appointmentId, Long newSlotId, Long userId);

    void cancelAppointment (Long appointmentId, Long userId);

}
