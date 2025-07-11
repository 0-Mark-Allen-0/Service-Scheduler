package com.example.SchedulerW4.dtos.auth_dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpRequestDto {
    @NotBlank
    private String email;

    @NotBlank
    private String otp;
}