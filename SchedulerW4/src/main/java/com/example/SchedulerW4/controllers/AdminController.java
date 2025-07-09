package com.example.SchedulerW4.controllers;


import com.example.SchedulerW4.dtos.AdminStatsDto;
import com.example.SchedulerW4.services.AdminService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    //Inject service
    private final AdminService adminService;
    private final Counter adminApiCallCounter;

    public AdminController(AdminService adminService, MeterRegistry meterRegistry) {
        this.adminService = adminService;
        this.adminApiCallCounter = Counter.builder("admin.api.requests.total")
                .description("Total requests to admin API endpoints")
                .tag("controller", "AdminController")
                .register(meterRegistry);
    }

    @GetMapping("/summary-stats")
    public ResponseEntity<AdminStatsDto> getSummaryStats() {
        adminApiCallCounter.increment(); // Increment on each call
        try {
            AdminStatsDto stats = adminService.getAppointmentStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching admin summary statistics", e);
        }
    }



}
