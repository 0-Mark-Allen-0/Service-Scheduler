package com.example.SchedulerW4.dtos.appointment_dtos;

import com.example.SchedulerW4.entities.Appointment;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponseDto {

    private Long appointmentId;
    private Long slotId;
    private String userName;
    private Long providerId;
    private String providerName;
    private String specialization;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Appointment.Status status;

}
