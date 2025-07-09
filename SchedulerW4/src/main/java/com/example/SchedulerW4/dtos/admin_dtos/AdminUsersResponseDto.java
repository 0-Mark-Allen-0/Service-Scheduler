package com.example.SchedulerW4.dtos.admin_dtos;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUsersResponseDto {
    private long totalCount;
    private List<AdminUserDto> users;

    public AdminUsersResponseDto (Page<AdminUserDto> page) {
        this.totalCount = page.getTotalElements();
        this.users = page.getContent();
    }
}
