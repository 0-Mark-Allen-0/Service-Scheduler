package com.example.SchedulerW4.dtos.response_dtos;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;
}
