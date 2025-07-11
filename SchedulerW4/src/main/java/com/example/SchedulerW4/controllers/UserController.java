package com.example.SchedulerW4.controllers;

import com.example.SchedulerW4.configs.SecurityUtils;
import com.example.SchedulerW4.dtos.MessageDto;
import com.example.SchedulerW4.dtos.appointment_dtos.AppointmentCancelDto;
import com.example.SchedulerW4.dtos.appointment_dtos.AppointmentRequestDto;
import com.example.SchedulerW4.dtos.appointment_dtos.AppointmentRescheduleRequestDto;
import com.example.SchedulerW4.dtos.appointment_dtos.AppointmentResponseDto;
import com.example.SchedulerW4.dtos.appointment_dtos.BookingResponseDto; // NEW IMPORT
import com.example.SchedulerW4.dtos.response_dtos.UserResponseDto;
import com.example.SchedulerW4.dtos.slot_dtos.SlotResponseDto;

import com.example.SchedulerW4.entities.User;
import com.example.SchedulerW4.services.AppointmentService;

import com.example.SchedulerW4.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus; // NEW IMPORT
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:5173/")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AppointmentService appointmentService;

//    //MOVE TO ADMIN
//    @GetMapping("/all")
//    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
//        List<UserResponseDto> responseDto = userService.getAllUsers().stream()
//                .filter(user -> user.getRole() == User.Role.USER)
//                .map(user -> UserResponseDto.builder()
//                        .id(user.getId())
//                        .name(user.getName())
//                        .email(user.getEmail())
//                        .build())
//                .toList();
//
//        return ResponseEntity.ok(responseDto);
//    }

    //Book an Appointment -- JWT
    @PostMapping("/appointment/book")
    public ResponseEntity<BookingResponseDto> bookAppointment( // MODIFIED RETURN TYPE
                                                               @RequestBody
                                                               @Valid
                                                               AppointmentRequestDto dto
    ) {
        User user = SecurityUtils.getCurrentUser();
        dto.setUserId(user.getId());

        BookingResponseDto response = appointmentService.bookAppointment(dto); // MODIFIED CALL

        // Determine HTTP status based on booking status
        if (response.getStatus() == BookingResponseDto.BookingStatus.BOOKED) {
            return new ResponseEntity<>(response, HttpStatus.CREATED); // 201 Created for a new booking
        } else if (response.getStatus() == BookingResponseDto.BookingStatus.QUEUED || response.getStatus() == BookingResponseDto.BookingStatus.ALREADY_QUEUED) {
            return new ResponseEntity<>(response, HttpStatus.OK); // 200 OK for being queued or already in queue
        } else {
            // This case should ideally be caught by exceptions from service, but as a fallback
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // Or other appropriate error status
        }
    }

    //Reschedule Appointment -- JWT
    @PostMapping("/appointment/reschedule")
    public ResponseEntity<BookingResponseDto> rescheduleAppointment ( // MODIFIED RETURN TYPE
                                                                      @RequestBody
                                                                      @Valid
                                                                      AppointmentRescheduleRequestDto dto
    ) {
        User user = SecurityUtils.getCurrentUser();

        BookingResponseDto response = appointmentService.rescheduleAppointment(dto.getAppointmentId(), dto.getNewSlotId(), user.getId()); // MODIFIED CALL

        // Determine HTTP status based on booking status
        if (response.getStatus() == BookingResponseDto.BookingStatus.BOOKED) {
            return new ResponseEntity<>(response, HttpStatus.OK); // 200 OK for a successful reschedule
        } else if (response.getStatus() == BookingResponseDto.BookingStatus.QUEUED) {
            return new ResponseEntity<>(response, HttpStatus.OK); // 200 OK for being queued for the new slot
        } else {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // Or other appropriate error status
        }
    }

    //Cancel an appointment -- JWT
    @DeleteMapping("/appointment/cancel")
    public ResponseEntity<MessageDto> cancelAppointment (
            @RequestBody
            @Valid
            AppointmentCancelDto dto
    ) {
        User user = SecurityUtils.getCurrentUser();
        appointmentService.cancelAppointment(dto.getAppointmentId(), user.getId());
        MessageDto responseDto = MessageDto.builder()
                .message("Appointment (ID: " + dto.getAppointmentId() + ") successfully cancelled")
                .build();
        return ResponseEntity.ok(responseDto);
    }

    //View available slots
    @GetMapping("/view/slots")
    public ResponseEntity<List<SlotResponseDto>> viewAllSlots () {
        List<SlotResponseDto> responseDto = userService.getAllSlots(); // Assuming this method exists in UserService
        return ResponseEntity.ok(responseDto);
    }

    //View your appointments
    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponseDto>> viewBookedSlots () {
        User user = SecurityUtils.getCurrentUser();
        List<AppointmentResponseDto> responseDto = appointmentService.getAllAppointments(user.getId());
        return ResponseEntity.ok(responseDto);
    }
}