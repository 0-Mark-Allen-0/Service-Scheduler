package com.example.SchedulerW4.dtos.admin_dtos;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserDto {

    private Long id;

    private String name;

    private String email;

}
