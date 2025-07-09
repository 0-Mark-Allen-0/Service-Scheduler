package com.example.SchedulerW4.services;

import com.example.SchedulerW4.dtos.AdminStatsDto;
import com.example.SchedulerW4.dtos.admin_dtos.AdminProviderDto;
import com.example.SchedulerW4.dtos.admin_dtos.AdminUserDto;
import com.example.SchedulerW4.dtos.admin_dtos.BusiestHourMetric;
import com.example.SchedulerW4.dtos.admin_dtos.PopularProviderMetric;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminService {

    AdminStatsDto getAppointmentStats ();

//    Page<AdminProviderDto> getAllProviders (String searchTerm, Pageable pageable);
//
//    long getTotalProviders ();
//
//    Page<AdminUserDto> getAllUsers (String searchTerm, Pageable pageable);
//
//    long getTotalUsers ();
//
//    long getTotalAppointmentsMade ();
//
//    public long getTotalAppointmentsCancelled ();
//
//    List<BusiestHourMetric> getBusiestHours (LocalDateTime startDate, LocalDateTime endDate);
//
//    PopularProviderMetric getMostPopularProvider (LocalDateTime startDate, LocalDateTime endDate);

}
