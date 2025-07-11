package com.example.SchedulerW4.Unit_Tests;

import com.example.SchedulerW4.dtos.AdminStatsDto;
import com.example.SchedulerW4.entities.Appointment;
import com.example.SchedulerW4.entities.Provider;
import com.example.SchedulerW4.entities.Slot;
import com.example.SchedulerW4.entities.User;
import com.example.SchedulerW4.repositories.AppointmentRepository;
import com.example.SchedulerW4.repositories.ProviderRepository;
import com.example.SchedulerW4.repositories.UserRepository;
import com.example.SchedulerW4.services.AdminService;
import com.example.SchedulerW4.services.AdminServiceImpl;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry; // Import SimpleMeterRegistry
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private UserRepository userRepository;

    // Use a real SimpleMeterRegistry instead of mocking MeterRegistry
    private MeterRegistry meterRegistry;

    private AdminService adminService;

    private Provider provider1;
    private Provider provider2;
    private User user;
    private Slot slot1;
    private Slot slot2;
    private Appointment appointment1;
    private Appointment appointment2;
    private Appointment appointment3;

    @BeforeEach
    void setUp() {
        // Initialize SimpleMeterRegistry here
        meterRegistry = new SimpleMeterRegistry();

        adminService = new AdminServiceImpl(appointmentRepository, providerRepository, userRepository, meterRegistry);

        provider1 = Provider.builder().id(1L).name("Dr. Smith").specialization("Cardiology").build();
        provider2 = Provider.builder().id(2L).name("Dr. Johnson").specialization("Neurology").build();

        user = User.builder().id(1L).name("John Doe").email("john@example.com").role(User.Role.USER).build();

        slot1 = Slot.builder()
                .id(1L)
                .startTime(LocalDateTime.of(2024, 1, 1, 9, 0))
                .endTime(LocalDateTime.of(2024, 1, 1, 10, 0))
                .provider(provider1)
                .build();

        slot2 = Slot.builder()
                .id(2L)
                .startTime(LocalDateTime.of(2024, 1, 1, 14, 0))
                .endTime(LocalDateTime.of(2024, 1, 1, 15, 0))
                .provider(provider2)
                .build();

        appointment1 = Appointment.builder()
                .id(1L).user(user).provider(provider1).slot(slot1).status(Appointment.Status.BOOKED).build();

        appointment2 = Appointment.builder()
                .id(2L).user(user).provider(provider1).slot(slot2).status(Appointment.Status.CANCELLED).build();

        appointment3 = Appointment.builder()
                .id(3L).user(user).provider(provider2).slot(slot2).status(Appointment.Status.BOOKED).build();
    }

    @Test
    void getAppointmentStats_ShouldReturnCorrectStats() {
        when(appointmentRepository.findAllWithProviderAndSlot()).thenReturn(List.of(appointment1, appointment2, appointment3));
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(providerRepository.count()).thenReturn(2L);

        AdminStatsDto result = adminService.getAppointmentStats();

        assertThat(result).isNotNull();
        assertThat(result.getTotalAppointmentsPerProvider()).hasSize(2);
        // Corrected expectation: Only BOOKED appointment1 for Dr. Smith is counted
        assertThat(result.getTotalAppointmentsPerProvider().get("Dr. Smith")).isEqualTo(1L);
        assertThat(result.getTotalAppointmentsPerProvider().get("Dr. Johnson")).isEqualTo(1L);

        assertThat(result.getCancellationRates().get("Dr. Smith")).isEqualTo(50.0);
        assertThat(result.getCancellationRates().get("Dr. Johnson")).isEqualTo(0.0);

        assertThat(result.getPeakBookingHours().get("09")).isEqualTo(1L);
        // Corrected expectation: Only BOOKED appointment3 for 14:00 is counted
        assertThat(result.getPeakBookingHours().get("14")).isEqualTo(1L);
        assertThat(result.getTotalUsers()).isEqualTo(1);
        assertThat(result.getTotalProviders()).isEqualTo(2);

        // Optional: Verify that metrics were recorded
        Timer recordedTimer = meterRegistry.find("admin.stats.processing.time").timer();
        assertThat(recordedTimer).isNotNull();
        assertThat(recordedTimer.count()).isEqualTo(1);

        Counter recordedCounter = meterRegistry.find("admin.stats.requests.total").tag("operation", "getStats").counter();
        assertThat(recordedCounter).isNotNull();
        assertThat(recordedCounter.count()).isEqualTo(1.0);
    }

    @Test
    void getAppointmentStats_WithEmptyAppointments_ShouldReturnEmptyStats() {
        when(appointmentRepository.findAllWithProviderAndSlot()).thenReturn(Collections.emptyList());
        when(userRepository.findAll()).thenReturn(List.of());
        when(providerRepository.count()).thenReturn(0L);

        AdminStatsDto result = adminService.getAppointmentStats();

        assertThat(result).isNotNull();
        assertThat(result.getTotalAppointmentsPerProvider()).isEmpty();
        assertThat(result.getCancellationRates()).isEmpty();
        assertThat(result.getPeakBookingHours()).isEmpty();
        assertThat(result.getTotalUsers()).isEqualTo(0);
        assertThat(result.getTotalProviders()).isEqualTo(0);
    }

    @Test
    void getAppointmentStats_WithNullSlot_ShouldNotBreakPeakHourStats() {
        Appointment appointmentWithNullSlot = Appointment.builder()
                .id(4L).user(user).provider(provider1).slot(null).status(Appointment.Status.BOOKED).build();

        when(appointmentRepository.findAllWithProviderAndSlot()).thenReturn(List.of(appointmentWithNullSlot));
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(providerRepository.count()).thenReturn(1L);

        AdminStatsDto result = adminService.getAppointmentStats();

        assertThat(result).isNotNull();
        assertThat(result.getTotalAppointmentsPerProvider().get("Dr. Smith")).isEqualTo(1L);
        assertThat(result.getCancellationRates().get("Dr. Smith")).isEqualTo(0.0);
        assertThat(result.getPeakBookingHours()).isEmpty();
    }

    @Test
    void getAppointmentStats_WithAllCancelled_ShouldReturn100PercentCancellation() {
        appointment1.setStatus(Appointment.Status.CANCELLED);
        when(appointmentRepository.findAllWithProviderAndSlot()).thenReturn(List.of(appointment1));
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(providerRepository.count()).thenReturn(1L);

        AdminStatsDto result = adminService.getAppointmentStats();

        assertThat(result).isNotNull();
        // Corrected expectation: Cancelled appointments are not counted in totalActiveOrCompletedAppointmentsPerProvider
        assertThat(result.getTotalAppointmentsPerProvider().get("Dr. Smith")).isEqualTo(0L);
        assertThat(result.getCancellationRates().get("Dr. Smith")).isEqualTo(100.0);
        assertThat(result.getTotalUsers()).isEqualTo(1);
        assertThat(result.getTotalProviders()).isEqualTo(1);
    }
}