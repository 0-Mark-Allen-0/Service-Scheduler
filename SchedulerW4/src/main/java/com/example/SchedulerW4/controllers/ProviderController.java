package com.example.SchedulerW4.controllers;


import com.example.SchedulerW4.configs.SecurityUtils;
import com.example.SchedulerW4.dtos.MessageDto;
import com.example.SchedulerW4.dtos.appointment_dtos.AppointmentResponseDto;
import com.example.SchedulerW4.dtos.response_dtos.ProviderResponseDto;
import com.example.SchedulerW4.dtos.slot_dtos.SlotDeleteDto;
import com.example.SchedulerW4.dtos.slot_dtos.SlotRequestDto;
import com.example.SchedulerW4.dtos.slot_dtos.SlotResponseDto;
import com.example.SchedulerW4.entities.Slot;
import com.example.SchedulerW4.entities.Provider;
import com.example.SchedulerW4.services.AppointmentService;
import com.example.SchedulerW4.services.ProviderService;
import com.example.SchedulerW4.services.SlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;
    private final SlotService slotService;
    private final AppointmentService appointmentService;

    //Shift to ADMIN
//    @GetMapping("/all")
//    public ResponseEntity<List<ProviderResponseDto>> getAllProviders() {
//        List<ProviderResponseDto> response = providerService.getAllProviders().stream()
//                .map(provider -> ProviderResponseDto.builder()
//                        .id(provider.getId())
//                        .name(provider.getName())
//                        .email(provider.getEmail())
//                        .specialization(provider.getSpecialization())
//                        .build())
//                .toList();
//
//        return ResponseEntity.ok(response);
//    }

    //View the slots the current provider has enrolled for -- JWT
    @GetMapping("/slots/enrolled")
    public ResponseEntity<List<SlotResponseDto>> getEnrolledSlots () {
        Provider provider = (Provider) SecurityUtils.getCurrentUser();

        List<SlotResponseDto> responseDto = slotService.getSlotsByProvider(provider);

        return ResponseEntity.ok(responseDto);
    }

    //Add a slot -- JWT
    @PostMapping("/slots/add")
    public ResponseEntity<SlotResponseDto> addSlot (
            @RequestBody
            @Valid
            SlotRequestDto dto
    ) {

        Provider provider = (Provider) SecurityUtils.getCurrentUser();

        Slot slot = slotService.createSlot(provider, dto.getStartTime());

        SlotResponseDto responseDto = SlotResponseDto.builder()
                .slotId(slot.getId())
                .providerId(provider.getId())
                .providerName(slot.getProvider().getName())
                .isBooked(slot.isBooked())
                .specialization(slot.getProvider().getSpecialization())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .build();

        return ResponseEntity.ok(responseDto);
    }

    //Delete slot -- JWT
    @DeleteMapping("/slots/delete")
    public ResponseEntity<MessageDto> deleteSlot (
            @RequestBody
            @Valid
            SlotDeleteDto dto
    ) {
        Provider provider = (Provider) SecurityUtils.getCurrentUser();

        if (!slotService.isOwner(provider, dto.getSlotId())) {
            throw new RuntimeException("UNAUTHORIZED: You can only delete your own slots");
        }

        slotService.deleteSlot(dto.getSlotId());

        MessageDto responseDto = MessageDto.builder().message("Slot deleted successfully!").build();

        return ResponseEntity.ok(responseDto);
    }

    //View the slots that have been booked -> Appointments
    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponseDto>> viewAllAppointments () {
        Provider provider = (Provider) SecurityUtils.getCurrentUser();

        List<AppointmentResponseDto> responseDto = appointmentService.getProviderAppointments(provider.getId());

        return ResponseEntity.ok(responseDto);
    }
}
