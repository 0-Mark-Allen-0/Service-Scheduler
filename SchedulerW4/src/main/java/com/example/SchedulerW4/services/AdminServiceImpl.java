package com.example.SchedulerW4.services;

import com.example.SchedulerW4.dtos.AdminStatsDto;
import com.example.SchedulerW4.dtos.appointment_dtos.AppointmentResponseDto;
import com.example.SchedulerW4.entities.Appointment;
import com.example.SchedulerW4.entities.Provider;
import com.example.SchedulerW4.entities.User;
import com.example.SchedulerW4.repositories.AppointmentRepository;
import com.example.SchedulerW4.repositories.ProviderRepository;
import com.example.SchedulerW4.repositories.UserRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private final AppointmentRepository appointmentRepository;
    private final ProviderRepository providerRepository;
    private final UserRepository userRepository;
    private final Timer adminStatsTimer;
    private final Counter adminStatsCounter;

    public AdminServiceImpl(AppointmentRepository appointmentRepository,
                            ProviderRepository providerRepository,
                            UserRepository userRepository,
                            MeterRegistry meterRegistry) {
        this.appointmentRepository = appointmentRepository;
        this.providerRepository = providerRepository;
        this.userRepository = userRepository;
        this.adminStatsTimer = Timer.builder("admin.stats.processing.time")
                .description("Time taken to process admin statistics")
                .register(meterRegistry);
        this.adminStatsCounter = Counter.builder("admin.stats.requests.total")
                .description("Total admin statistics requests")
                .tag("operation", "getStats")
                .register(meterRegistry);
    }

    @Override
    @Cacheable(value = "adminStats", key = "'dashboardStats'", unless = "#result == null")
    public AdminStatsDto getAppointmentStats() {
        return adminStatsTimer.record(() -> {
            adminStatsCounter.increment();

            List<Appointment> allAppointments = appointmentRepository.findAllWithProviderAndSlot();

            // Get total counts
            long totalUsers = userRepository.findAll().stream()
                    .filter(user -> user.getRole() == User.Role.USER)
                    .count();
            long totalProviders = providerRepository.count();

            Map<String, Long> totalActiveOrCompletedAppointmentsPerProvider = new HashMap<>();
            Map<String, Double> cancellationRates = new HashMap<>();
            Map<String, Long> peakBookingHours = new TreeMap<>();

            Map<Provider, List<Appointment>> appointmentsByProvider = allAppointments.stream()
                    .collect(Collectors.groupingBy(Appointment::getProvider));

            for (Map.Entry<Provider, List<Appointment>> entry : appointmentsByProvider.entrySet()) {
                Provider provider = entry.getKey();
                List<Appointment> appointmentsForProvider = entry.getValue();

                long totalAppointmentsEver = appointmentsForProvider.size();
                long cancelledAppointments = appointmentsForProvider.stream()
                        .filter(a -> a.getStatus() == Appointment.Status.CANCELLED)
                        .count();

                long activeOrCompleted = appointmentsForProvider.stream()
                        .filter(a -> a.getStatus() != Appointment.Status.CANCELLED)
                        .count();

                totalActiveOrCompletedAppointmentsPerProvider.put(provider.getName(), activeOrCompleted);

                double rate = (totalAppointmentsEver > 0) ?
                        (double) cancelledAppointments / totalAppointmentsEver * 100.0 : 0.0;
                cancellationRates.put(provider.getName(), rate);
            }

            for (Appointment appointment : allAppointments) {
                if (appointment.getStatus() != Appointment.Status.CANCELLED &&
                        appointment.getSlot() != null &&
                        appointment.getSlot().getStartTime() != null) {
                    int hour = appointment.getSlot().getStartTime().getHour();
                    String hourKey = String.format("%02d", hour);
                    peakBookingHours.merge(hourKey, 1L, Long::sum);
                }
            }

            return AdminStatsDto.builder()
                    .totalAppointmentsPerProvider(totalActiveOrCompletedAppointmentsPerProvider)
                    .cancellationRates(cancellationRates)
                    .peakBookingHours(peakBookingHours)
                    .totalUsers(totalUsers) // NEW
                    .totalProviders(totalProviders) // NEW
                    .build();
        });
    }

    @Override
    public List<AppointmentResponseDto> getAllAppointments() {
        List<Appointment> appointments = appointmentRepository.findAllWithProviderAndSlot();
        return appointments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AppointmentResponseDto mapToResponse(Appointment appointment) {
        return AppointmentResponseDto.builder()
                .appointmentId(appointment.getId())
                .slotId(appointment.getSlot().getId())
                .userName(appointment.getUser().getName())
                .providerId(appointment.getProvider().getId())
                .providerName(appointment.getProvider().getName())
                .specialization(appointment.getProvider().getSpecialization())
                .startTime(appointment.getSlot().getStartTime())
                .endTime(appointment.getSlot().getStartTime().plusHours(1))
                .status(Appointment.Status.valueOf(appointment.getStatus().name()))
                .build();
    }
}
