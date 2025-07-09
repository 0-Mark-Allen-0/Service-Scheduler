package com.example.SchedulerW4.dtos.admin_dtos;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PopularProviderMetric {

    private AdminProviderDto provider;
    private Long totalAppointments;

}
