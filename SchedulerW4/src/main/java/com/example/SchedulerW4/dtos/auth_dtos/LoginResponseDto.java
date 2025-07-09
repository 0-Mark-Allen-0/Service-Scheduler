package com.example.SchedulerW4.dtos.auth_dtos;

import com.example.SchedulerW4.entities.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDto {
    private String message;
    private User.Role role;
    private String token;
}
