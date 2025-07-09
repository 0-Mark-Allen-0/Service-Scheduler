package com.example.SchedulerW4.services;

import com.example.SchedulerW4.dtos.appointment_dtos.AppointmentResponseDto;
import com.example.SchedulerW4.dtos.auth_dtos.UserRegistrationDto;
import com.example.SchedulerW4.dtos.response_dtos.ProviderResponseDto;
import com.example.SchedulerW4.dtos.slot_dtos.SlotResponseDto;
import com.example.SchedulerW4.entities.User;
import com.example.SchedulerW4.repositories.SlotRepository;
import com.example.SchedulerW4.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{


    //Inject Repository
    private final UserRepository userRepository;
    private final SlotRepository slotRepository;


    private final PasswordEncoder passwordEncoder;


    @Override
    public User registerUser(UserRegistrationDto dto) {

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(User.Role.USER)
                .build();

        return userRepository.save(user);

    }

    @Override
    public User findById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User Not Found With ID: " + id));

    }

    @Override
    public User findByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User Not Found With Email: " + email));

    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<SlotResponseDto> getAllSlots() {
        List<SlotResponseDto> responseDto = slotRepository.findAll().stream()
                .map(slot -> SlotResponseDto.builder()
                        .slotId(slot.getId())
                        .providerId(slot.getProvider().getId())
                        .providerName(slot.getProvider().getName())
                        .specialization(slot.getProvider().getSpecialization())
                        .startTime(slot.getStartTime())
                        .endTime(slot.getEndTime())
                        .isBooked(slot.isBooked())
                        .build())
                .toList();

        return responseDto;

    }

    @Override
    public List<SlotResponseDto> getAllAvailableSlots() {
        List<SlotResponseDto> responseDto = slotRepository.findByIsBooked(false).stream() // Use a custom finder method
                .map(slot -> SlotResponseDto.builder()
                        .slotId(slot.getId())
                        .providerId(slot.getProvider().getId())
                        .providerName(slot.getProvider().getName())
                        .specialization(slot.getProvider().getSpecialization())
                        .startTime(slot.getStartTime())
                        .endTime(slot.getEndTime())
                        .isBooked(slot.isBooked())
                        .build())
                .toList();
        return responseDto;
    }
}
