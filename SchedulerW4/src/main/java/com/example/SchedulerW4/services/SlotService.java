package com.example.SchedulerW4.services;

import com.example.SchedulerW4.dtos.slot_dtos.SlotResponseDto;
import com.example.SchedulerW4.entities.Slot;
import com.example.SchedulerW4.entities.Provider;

import java.time.LocalDateTime;
import java.util.List;

public interface SlotService {

    List<SlotResponseDto> getSlotsByProvider(Provider provider);

    Slot findById (Long id);

    Slot createSlot (Provider provider, LocalDateTime startTime);

    void deleteSlot (Long slotId);

    boolean isOwner (Provider provider, Long slotId);

}
