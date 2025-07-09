package com.example.SchedulerW4.services;

import com.example.SchedulerW4.dtos.appointment_dtos.AppointmentResponseDto;
import com.example.SchedulerW4.dtos.auth_dtos.UserRegistrationDto;
import com.example.SchedulerW4.dtos.response_dtos.ProviderResponseDto;
import com.example.SchedulerW4.dtos.slot_dtos.SlotResponseDto;
import com.example.SchedulerW4.entities.Slot;
import com.example.SchedulerW4.entities.User;

import java.util.List;

public interface UserService {

    User registerUser (UserRegistrationDto dto);

    User findById (Long id);

    User findByEmail (String email);

    List<User> getAllUsers ();

    List<SlotResponseDto> getAllSlots ();

    List<SlotResponseDto> getAllAvailableSlots ();

//    List<ProviderResponseDto> getAllProviders ();
//
//    List<AppointmentResponseDto> getAllAppointments (Long userId);

}
