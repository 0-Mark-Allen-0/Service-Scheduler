package com.example.SchedulerW4.dtos.auth_dtos;

import com.example.SchedulerW4.entities.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDto {
    private String message;
    private User.Role role; // This might be null if OTP is required
    private String token; // This will be null if OTP is required
    private boolean otpRequired; // NEW: Indicates if OTP is required for login
    private String email; // NEW: The email for which OTP was sent (useful for frontend)
}