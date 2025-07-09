package com.example.SchedulerW4.services;

import com.example.SchedulerW4.dtos.slot_dtos.SlotResponseDto;
import com.example.SchedulerW4.entities.Slot;
import com.example.SchedulerW4.entities.Provider;
import com.example.SchedulerW4.repositories.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class SlotServiceImpl implements SlotService{

    //Inject Repository
    private final SlotRepository slotRepository;

    @Override
    public List<SlotResponseDto> getSlotsByProvider (Provider provider) {
        return slotRepository.findByProviderId(provider.getId()).stream()
                .map(slot -> SlotResponseDto.builder()
                                .slotId(slot.getId())
                                .startTime(slot.getStartTime())
                                .endTime(slot.getEndTime())
                                .isBooked(slot.isBooked())
                                .build()
                                )
                .toList();
    }

    @Override
    public Slot findById(Long id) {

        return slotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Slot Not Found For ID: " + id));
    }

    @Override
    public Slot createSlot(Provider provider, LocalDateTime startTime) {
        boolean exists = slotRepository.existsByProviderAndStartTime(provider, startTime);

        List<Slot> existingSlots = slotRepository.findByProviderId(provider.getId());

        boolean clashing = existingSlots.stream().anyMatch(existingSlot ->
                            existingSlot.getStartTime().isBefore(startTime.plusHours(1)) &&
                            startTime.isBefore(existingSlot.getEndTime())
                            );
        if (clashing) {
            throw new IllegalArgumentException("Slot already exists!");
        }

        Slot slot = Slot.builder()
                .provider(provider)
                .startTime(startTime)
                .endTime(startTime.plusHours(1))
                .isBooked(false)
                .build();

        return slotRepository.save(slot);
    }

    @Override
    public void deleteSlot(Long slotId) {
        Slot slot = slotRepository.findById(slotId).orElseThrow(() -> new RuntimeException("Slot doesn't exist"));
        slotRepository.delete(slot);
    }

    @Override
    public boolean isOwner(Provider provider, Long slotId) {

        return slotRepository.existsByIdAndProviderId(slotId, provider.getId());

    }


}
