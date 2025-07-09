package com.example.SchedulerW4.Unit_Tests;

import com.example.SchedulerW4.dtos.AdminStatsDto;
import com.example.SchedulerW4.entities.Appointment;
import com.example.SchedulerW4.entities.Provider;
import com.example.SchedulerW4.entities.Slot;
import com.example.SchedulerW4.entities.User;
import com.example.SchedulerW4.repositories.AppointmentRepository;
import com.example.SchedulerW4.repositories.ProviderRepository;
import com.example.SchedulerW4.services.AdminService;
import com.example.SchedulerW4.services.AdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ProviderRepository providerRepository;

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
        adminService = new AdminServiceImpl(appointmentRepository, providerRepository);

        provider1 = Provider.builder()
                .id(1L)
                .name("Dr. Smith")
                .specialization("Cardiology")
                .build();

        provider2 = Provider.builder()
                .id(2L)
                .name("Dr. Johnson")
                .specialization("Neurology")
                .build();

        user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

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
                .id(1L)
                .user(user)
                .provider(provider1)
                .slot(slot1)
                .status(Appointment.Status.BOOKED)
                .build();

        appointment2 = Appointment.builder()
                .id(2L)
                .user(user)
                .provider(provider1)
                .slot(slot2)
                .status(Appointment.Status.CANCELLED)
                .build();

        appointment3 = Appointment.builder()
                .id(3L)
                .user(user)
                .provider(provider2)
                .slot(slot2)
                .status(Appointment.Status.BOOKED)
                .build();
    }

    @Test
    void getAppointmentStats_ShouldReturnCorrectStats() {
        List<Appointment> appointments = Arrays.asList(appointment1, appointment2, appointment3);
        when(appointmentRepository.findAllWithProviderAndSlot()).thenReturn(appointments);

        AdminStatsDto result = adminService.getAppointmentStats();

        assertThat(result).isNotNull();
        assertThat(result.getTotalAppointmentsPerProvider()).hasSize(2);
        assertThat(result.getTotalAppointmentsPerProvider().get("Dr. Smith")).isEqualTo(2L);
        assertThat(result.getTotalAppointmentsPerProvider().get("Dr. Johnson")).isEqualTo(1L);

        assertThat(result.getCancellationRates().get("Dr. Smith")).isEqualTo(50.0);
        assertThat(result.getCancellationRates().get("Dr. Johnson")).isEqualTo(0.0);

        assertThat(result.getPeakBookingHours().get("09:00")).isEqualTo(1L);
        assertThat(result.getPeakBookingHours().get("14:00")).isEqualTo(2L);
    }

    @Test
    void getAppointmentStats_WithEmptyAppointments_ShouldReturnEmptyStats() {
        when(appointmentRepository.findAllWithProviderAndSlot()).thenReturn(Collections.emptyList());

        AdminStatsDto result = adminService.getAppointmentStats();

        assertThat(result).isNotNull();
        assertThat(result.getTotalAppointmentsPerProvider()).isEmpty();
        assertThat(result.getCancellationRates()).isEmpty();
        assertThat(result.getPeakBookingHours()).isEmpty();
    }

    @Test
    void getAppointmentStats_WithNullSlot_ShouldNotBreakPeakHourStats() {
        Appointment appointmentWithNullSlot = Appointment.builder()
                .id(4L)
                .user(user)
                .provider(provider1)
                .slot(null)
                .status(Appointment.Status.BOOKED)
                .build();

        when(appointmentRepository.findAllWithProviderAndSlot())
                .thenReturn(Collections.singletonList(appointmentWithNullSlot));

        AdminStatsDto result = adminService.getAppointmentStats();

        assertThat(result).isNotNull();
        assertThat(result.getTotalAppointmentsPerProvider().get("Dr. Smith")).isEqualTo(1L);
        assertThat(result.getCancellationRates().get("Dr. Smith")).isEqualTo(0.0);
        assertThat(result.getPeakBookingHours()).isEmpty(); // no hour extracted from null slot
    }

    @Test
    void getAppointmentStats_WithAllCancelled_ShouldReturn100PercentCancellation() {
        appointment1.setStatus(Appointment.Status.CANCELLED);
        when(appointmentRepository.findAllWithProviderAndSlot())
                .thenReturn(Collections.singletonList(appointment1));

        AdminStatsDto result = adminService.getAppointmentStats();

        assertThat(result).isNotNull();
        assertThat(result.getTotalAppointmentsPerProvider().get("Dr. Smith")).isEqualTo(1L);
        assertThat(result.getCancellationRates().get("Dr. Smith")).isEqualTo(100.0);
    }
}
