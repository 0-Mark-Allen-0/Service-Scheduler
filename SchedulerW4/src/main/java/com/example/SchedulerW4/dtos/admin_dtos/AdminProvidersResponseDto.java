package com.example.SchedulerW4.dtos.admin_dtos;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminProvidersResponseDto {

    private Long totalCount;
    private List<AdminProviderDto> providers;

    public AdminProvidersResponseDto (Page<AdminProviderDto> page) {
        this.totalCount = page.getTotalElements();
        this.providers = page.getContent();
    }
}
