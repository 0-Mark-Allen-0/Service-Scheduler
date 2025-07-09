package com.example.SchedulerW4.dtos.appointment_dtos;


import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentRequestDto {

    @NotNull
    private Long userId;

    @NotNull
    private Long providerId;

    @NotNull
    private Long slotId;

}
