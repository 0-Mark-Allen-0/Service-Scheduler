package com.example.SchedulerW4.repositories;

import com.example.SchedulerW4.dtos.slot_dtos.SlotResponseDto;
import com.example.SchedulerW4.entities.Slot;
import com.example.SchedulerW4.entities.Provider;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SlotRepository extends JpaRepository<Slot, Long> {

    List<Slot> findByProviderId (Long providerId);

    List<Slot> findByIsBooked (boolean isBooked);

    boolean existsByProviderAndStartTime(Provider provider, LocalDateTime startTime);

    boolean existsByIdAndProviderId(Long slotId, Long providerId);

    List<Slot> findByIsBookedFalseAndEndTimeBefore(LocalDateTime time);

}
