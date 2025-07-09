package com.example.SchedulerW4.dtos.appointment_dtos;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentCancelDto {

    @NotNull
    private Long appointmentId;

}
