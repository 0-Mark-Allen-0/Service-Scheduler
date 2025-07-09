package com.example.SchedulerW4.dtos.admin_dtos;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminProviderDto {

    private Long id;

    private String name;

    private String email;

    private String specialization;

    private Long totalAppointments;

}
