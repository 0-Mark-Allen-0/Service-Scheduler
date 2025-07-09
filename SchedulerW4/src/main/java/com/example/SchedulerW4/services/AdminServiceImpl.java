package com.example.SchedulerW4.services;

import com.example.SchedulerW4.dtos.AdminStatsDto;
import com.example.SchedulerW4.entities.Appointment;
import com.example.SchedulerW4.entities.Provider;
import com.example.SchedulerW4.repositories.AppointmentRepository;
import com.example.SchedulerW4.repositories.ProviderRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private final AppointmentRepository appointmentRepository;
    private final ProviderRepository providerRepository;

    public AdminServiceImpl(AppointmentRepository appointmentRepository,
                            ProviderRepository providerRepository) {
        this.appointmentRepository = appointmentRepository;
        this.providerRepository = providerRepository;
    }

    @Override
    @Cacheable(value = "adminStats", key = "'dashboardStats'", unless = "#result == null")
    public AdminStatsDto getAppointmentStats() {
        List<Appointment> allAppointments = appointmentRepository.findAllWithProviderAndSlot();

        Map<String, Long> totalAppointmentsPerProvider = new HashMap<>();
        Map<String, Double> cancellationRates = new HashMap<>();
        Map<String, Long> peakBookingHours = new TreeMap<>();

        Map<Provider, List<Appointment>> appointmentsByProvider = allAppointments.stream()
                .collect(Collectors.groupingBy(Appointment::getProvider));

        for (Map.Entry<Provider, List<Appointment>> entry : appointmentsByProvider.entrySet()) {
            Provider provider = entry.getKey();
            List<Appointment> appointmentsForProvider = entry.getValue();

            long total = appointmentsForProvider.size();
            long cancelled = appointmentsForProvider.stream()
                    .filter(a -> a.getStatus() == Appointment.Status.CANCELLED)
                    .count();

            totalAppointmentsPerProvider.put(provider.getName(), total);
            double rate = (total > 0) ? (double) cancelled / total * 100.0 : 0.0;
            cancellationRates.put(provider.getName(), rate);
        }

        for (Appointment appointment : allAppointments) {
            if (appointment.getSlot() != null && appointment.getSlot().getStartTime() != null) {
                int hour = appointment.getSlot().getStartTime().getHour();
                String hourKey = String.format("%02d:00", hour);
                peakBookingHours.merge(hourKey, 1L, Long::sum);
            }
        }

        return AdminStatsDto.builder()
                .totalAppointmentsPerProvider(totalAppointmentsPerProvider)
                .cancellationRates(cancellationRates)
                .peakBookingHours(peakBookingHours)
                .build();
    }
}
