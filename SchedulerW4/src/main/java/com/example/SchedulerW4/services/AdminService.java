package com.example.SchedulerW4.services;

import com.example.SchedulerW4.dtos.AdminStatsDto;
import com.example.SchedulerW4.dtos.appointment_dtos.AppointmentResponseDto;
import java.util.List;

public interface AdminService {
    AdminStatsDto getAppointmentStats();
    List<AppointmentResponseDto> getAllAppointments(); // NEW method
}
