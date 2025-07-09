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
        // Ensure this fetches the current state, including updated slot for rescheduled appointments
        List<Appointment> allAppointments = appointmentRepository.findAllWithProviderAndSlot();

        Map<String, Long> totalActiveOrCompletedAppointmentsPerProvider = new HashMap<>(); // Renamed for clarity
        Map<String, Double> cancellationRates = new HashMap<>();
        Map<String, Long> peakBookingHours = new TreeMap<>();

        Map<Provider, List<Appointment>> appointmentsByProvider = allAppointments.stream()
                .collect(Collectors.groupingBy(Appointment::getProvider));

        for (Map.Entry<Provider, List<Appointment>> entry : appointmentsByProvider.entrySet()) {
            Provider provider = entry.getKey();
            List<Appointment> appointmentsForProvider = entry.getValue();

            long totalAppointmentsEver = appointmentsForProvider.size(); // Total appointments made, including cancelled
            long cancelledAppointments = appointmentsForProvider.stream()
                    .filter(a -> a.getStatus() == Appointment.Status.CANCELLED)
                    .count();
            // Count active/completed appointments for this provider
            long activeOrCompleted = appointmentsForProvider.stream()
                    .filter(a -> a.getStatus() != Appointment.Status.CANCELLED)
                    .count();

            totalActiveOrCompletedAppointmentsPerProvider.put(provider.getName(), activeOrCompleted); // Store active/completed
            double rate = (totalAppointmentsEver > 0) ? (double) cancelledAppointments / totalAppointmentsEver * 100.0 : 0.0;
            cancellationRates.put(provider.getName(), rate);
        }

        for (Appointment appointment : allAppointments) {
            // Only consider active/booked appointments for peak hours
            if (appointment.getStatus() != Appointment.Status.CANCELLED && appointment.getSlot() != null && appointment.getSlot().getStartTime() != null) {
                int hour = appointment.getSlot().getStartTime().getHour();
                String hourKey = String.format("%02d:00", hour);
                peakBookingHours.merge(hourKey, 1L, Long::sum);
            }
        }

        return AdminStatsDto.builder()
                .totalAppointmentsPerProvider(totalActiveOrCompletedAppointmentsPerProvider) // Use the new map
                .cancellationRates(cancellationRates)
                .peakBookingHours(peakBookingHours)
                .build();
    }
}