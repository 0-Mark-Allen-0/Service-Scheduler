package com.example.SchedulerW4.dtos.admin_dtos;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusiestHourMetric {
    private String hour;
    private Long count;
}
