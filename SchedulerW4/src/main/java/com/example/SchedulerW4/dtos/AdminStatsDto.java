package com.example.SchedulerW4.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminStatsDto {

    private Map<String, Long> totalAppointmentsPerProvider;

    private Map<String, Double> cancellationRates;

    private Map<String, Long> peakBookingHours;

}
