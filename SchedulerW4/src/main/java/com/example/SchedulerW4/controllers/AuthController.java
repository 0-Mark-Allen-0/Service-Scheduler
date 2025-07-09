package com.example.SchedulerW4.controllers;

import com.example.SchedulerW4.configs.AuthenticationService;
import com.example.SchedulerW4.dtos.auth_dtos.LoginRequestDto;
import com.example.SchedulerW4.dtos.auth_dtos.LoginResponseDto;
import com.example.SchedulerW4.dtos.auth_dtos.ProviderRegistrationDto;
import com.example.SchedulerW4.dtos.auth_dtos.UserRegistrationDto;
import com.example.SchedulerW4.dtos.response_dtos.UserResponseDto;
import com.example.SchedulerW4.dtos.response_dtos.ProviderResponseDto;
import com.example.SchedulerW4.entities.Provider;
import com.example.SchedulerW4.entities.User;
import com.example.SchedulerW4.services.ProviderService;
import com.example.SchedulerW4.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")
//FIXED & READY

public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final ProviderService providerService;

    //Centralized all authorizations within Auth Controller

    //User Registration
    @PostMapping("/register/user")
    public ResponseEntity<UserResponseDto> registerUser(@RequestBody @Valid UserRegistrationDto dto) {

        User newUser = userService.registerUser(dto);

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(newUser.getId())
                .name(newUser.getName())
                .email(newUser.getEmail())
                .build();

        return ResponseEntity.ok(responseDto);

    }

    //Provider Registration
    @PostMapping("/register/provider")
    public ResponseEntity<ProviderResponseDto> registerProvider(@RequestBody @Valid ProviderRegistrationDto dto) {

        Provider newProvider = providerService.registerProvider(dto);

        ProviderResponseDto responseDto = ProviderResponseDto.builder()
                .id(newProvider.getId())
                .name(newProvider.getName())
                .email(newProvider.getEmail())
                .specialization(newProvider.getSpecialization())
                .build();

        return ResponseEntity.ok(responseDto);

    }

    //User + Provider + Admin Login
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login (
            @RequestBody @Valid LoginRequestDto loginDto
    ) {
        // Now, authenticationService.authenticate returns the full LoginResponseDto
        LoginResponseDto responseDto = authenticationService.authenticate(loginDto);
        return ResponseEntity.ok(responseDto);
    }

}
