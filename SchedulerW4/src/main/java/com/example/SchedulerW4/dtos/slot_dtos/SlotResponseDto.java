package com.example.SchedulerW4.dtos.slot_dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SlotResponseDto {

    private Long slotId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isBooked;

    private Long providerId;
    private String providerName;
    private String specialization;

}
