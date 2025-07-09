package com.example.SchedulerW4.dtos.slot_dtos;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SlotDeleteDto {

    @NotNull
    private Long slotId;

}
