package com.example.SchedulerW4.controllers;

import com.example.SchedulerW4.dtos.AdminStatsDto;
import com.example.SchedulerW4.dtos.appointment_dtos.AppointmentResponseDto;
import com.example.SchedulerW4.dtos.response_dtos.UserResponseDto;
import com.example.SchedulerW4.dtos.response_dtos.ProviderResponseDto;
import com.example.SchedulerW4.dtos.slot_dtos.SlotResponseDto;
import com.example.SchedulerW4.entities.User;
import com.example.SchedulerW4.services.AdminService;
import com.example.SchedulerW4.services.UserService;
import com.example.SchedulerW4.services.ProviderService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:5173/")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;
    private final ProviderService providerService;
    private final HealthEndpoint healthEndpoint;
    private final MeterRegistry meterRegistry;
    private final Counter adminApiCallCounter;

    // Single constructor - removed @RequiredArgsConstructor and using manual constructor
    public AdminController(AdminService adminService,
                           UserService userService,
                           ProviderService providerService,
                           HealthEndpoint healthEndpoint,
                           MeterRegistry meterRegistry) {
        this.adminService = adminService;
        this.userService = userService;
        this.providerService = providerService;
        this.healthEndpoint = healthEndpoint;
        this.meterRegistry = meterRegistry;

        // Initialize the counter
        this.adminApiCallCounter = Counter.builder("admin.api.requests.total")
                .description("Total requests to admin API endpoints")
                .tag("controller", "AdminController")
                .register(meterRegistry);
    }

    @GetMapping("/summary-stats")
    public ResponseEntity<AdminStatsDto> getSummaryStats() {
        System.out.println("=== Admin Controller Debug ===");
        System.out.println("getSummaryStats() called");
        System.out.println("Current authentication: " + SecurityContextHolder.getContext().getAuthentication());
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            System.out.println("Authorities: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        }
        System.out.println("=== End Admin Controller Debug ===");

        adminApiCallCounter.increment();
        try {
            AdminStatsDto stats = adminService.getAppointmentStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.out.println("Error in getSummaryStats: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching admin summary statistics", e);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        adminApiCallCounter.increment();
        List<UserResponseDto> responseDto = userService.getAllUsers().stream()
                .filter(user -> user.getRole() == User.Role.USER)
                .map(user -> UserResponseDto.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .build())
                .toList();
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/providers")
    public ResponseEntity<List<ProviderResponseDto>> getAllProviders() {
        adminApiCallCounter.increment();
        List<ProviderResponseDto> response = providerService.getAllProviders().stream()
                .map(provider -> ProviderResponseDto.builder()
                        .id(provider.getId())
                        .name(provider.getName())
                        .email(provider.getEmail())
                        .specialization(provider.getSpecialization())
                        .build())
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponseDto>> getAllAppointments() {
        adminApiCallCounter.increment();
        List<AppointmentResponseDto> appointments = adminService.getAllAppointments();
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/slots")
    public ResponseEntity<List<SlotResponseDto>> getAllSlots() {
        adminApiCallCounter.increment();
        List<SlotResponseDto> slots = userService.getAllSlots();
        return ResponseEntity.ok(slots);
    }

//    @GetMapping("/health-metrics")
//    public ResponseEntity<Map<String, Object>> getHealthMetrics() {
//        adminApiCallCounter.increment();
//        try {
//            Map<String, Object> healthMetrics = new HashMap<>();
//
//            // Get overall health status
//            var health = healthEndpoint.health();
//            healthMetrics.put("overallStatus", health.getStatus().getCode());
//
//            // Check if health has components
//            if (health.getComponents() != null && !health.getComponents().isEmpty()) {
//                healthMetrics.put("database", health.getComponents().get("db"));
//                healthMetrics.put("redis", health.getComponents().get("redis"));
//                healthMetrics.put("diskSpace", health.getComponents().get("diskSpace"));
//            }
//
//            // Get basic JVM metrics from MeterRegistry
//            try {
//                // Get some basic metrics using MeterRegistry
//                var memoryUsed = meterRegistry.get("jvm.memory.used").gauge().value();
//                var memoryMax = meterRegistry.get("jvm.memory.max").gauge().value();
//
//                healthMetrics.put("jvmMemoryUsed", memoryUsed);
//                healthMetrics.put("jvmMemoryMax", memoryMax);
//                healthMetrics.put("memoryUsagePercent", (memoryUsed / memoryMax) * 100);
//
//            } catch (Exception e) {
//                // Metrics might not be available, continue without them
//                healthMetrics.put("metricsError", "Some metrics unavailable: " + e.getMessage());
//            }
//
//            return ResponseEntity.ok(healthMetrics);
//        } catch (Exception e) {
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching health metrics", e);
//        }
//    }
}
