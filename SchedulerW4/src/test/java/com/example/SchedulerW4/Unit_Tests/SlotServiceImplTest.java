package com.example.SchedulerW4.Unit_Tests;

import com.example.SchedulerW4.dtos.slot_dtos.SlotResponseDto;
import com.example.SchedulerW4.entities.Provider;
import com.example.SchedulerW4.entities.Slot;
import com.example.SchedulerW4.repositories.SlotRepository;
import com.example.SchedulerW4.services.SlotServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SlotServiceImplTest {

    @Mock
    private SlotRepository slotRepository;

    @InjectMocks
    private SlotServiceImpl slotService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getSlotsByProvider_shouldReturnMappedSlots() {
        Provider provider = Provider.builder().id(1L).build();
        Slot slot = Slot.builder()
                .id(10L)
                .startTime(LocalDateTime.of(2025, 7, 8, 10, 0))
                .endTime(LocalDateTime.of(2025, 7, 8, 11, 0))
                .isBooked(true)
                .build();

        when(slotRepository.findByProviderId(1L)).thenReturn(List.of(slot));

        List<SlotResponseDto> result = slotService.getSlotsByProvider(provider);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSlotId()).isEqualTo(10L);
        assertThat(result.get(0).isBooked()).isTrue();
    }

    @Test
    void findById_validId_shouldReturnSlot() {
        Slot slot = Slot.builder().id(5L).build();
        when(slotRepository.findById(5L)).thenReturn(Optional.of(slot));

        Slot result = slotService.findById(5L);

        assertThat(result.getId()).isEqualTo(5L);
    }

    @Test
    void findById_invalidId_shouldThrow() {
        when(slotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> slotService.findById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Slot Not Found For ID");
    }

    @Test
    void createSlot_whenClashingSlotExists_shouldThrow() {
        Provider provider = Provider.builder().id(1L).build();
        LocalDateTime startTime = LocalDateTime.of(2025, 7, 8, 10, 0);

        Slot existingSlot = Slot.builder()
                .startTime(startTime.minusMinutes(30))
                .endTime(startTime.plusMinutes(30))
                .build();

        when(slotRepository.existsByProviderAndStartTime(provider, startTime)).thenReturn(false);
        when(slotRepository.findByProviderId(1L)).thenReturn(List.of(existingSlot));

        assertThatThrownBy(() -> slotService.createSlot(provider, startTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Slot already exists!");
    }

    @Test
    void createSlot_validSlot_shouldSaveAndReturnSlot() {
        Provider provider = Provider.builder().id(1L).build();
        LocalDateTime startTime = LocalDateTime.of(2025, 7, 8, 14, 0);

        when(slotRepository.existsByProviderAndStartTime(provider, startTime)).thenReturn(false);
        when(slotRepository.findByProviderId(1L)).thenReturn(List.of());

        Slot newSlot = Slot.builder()
                .id(20L)
                .provider(provider)
                .startTime(startTime)
                .endTime(startTime.plusHours(1))
                .isBooked(false)
                .build();

        when(slotRepository.save(any(Slot.class))).thenReturn(newSlot);

        Slot result = slotService.createSlot(provider, startTime);

        assertThat(result.getId()).isEqualTo(20L);
        assertThat(result.getStartTime()).isEqualTo(startTime);
        assertThat(result.getEndTime()).isEqualTo(startTime.plusHours(1));
    }

    @Test
    void deleteSlot_validId_shouldDeleteSlot() {
        Slot slot = Slot.builder().id(10L).build();
        when(slotRepository.findById(10L)).thenReturn(Optional.of(slot));

        slotService.deleteSlot(10L);

        verify(slotRepository).delete(slot);
    }

    @Test
    void deleteSlot_invalidId_shouldThrow() {
        when(slotRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> slotService.deleteSlot(404L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Slot doesn't exist");
    }

    @Test
    void isOwner_shouldReturnTrueWhenSlotBelongsToProvider() {
        Provider provider = Provider.builder().id(3L).build();
        when(slotRepository.existsByIdAndProviderId(15L, 3L)).thenReturn(true);

        boolean result = slotService.isOwner(provider, 15L);

        assertThat(result).isTrue();
    }

    @Test
    void isOwner_shouldReturnFalseWhenSlotDoesNotBelongToProvider() {
        Provider provider = Provider.builder().id(3L).build();
        when(slotRepository.existsByIdAndProviderId(15L, 3L)).thenReturn(false);

        boolean result = slotService.isOwner(provider, 15L);

        assertThat(result).isFalse();
    }
}
