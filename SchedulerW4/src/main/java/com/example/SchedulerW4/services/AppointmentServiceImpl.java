package com.example.SchedulerW4.services;

import com.example.SchedulerW4.dtos.appointment_dtos.AppointmentRequestDto;
import com.example.SchedulerW4.dtos.appointment_dtos.AppointmentResponseDto;
import com.example.SchedulerW4.dtos.notification_dtos.NotificationDto;
import com.example.SchedulerW4.entities.Appointment;
import com.example.SchedulerW4.entities.Slot;
import com.example.SchedulerW4.entities.Provider;
import com.example.SchedulerW4.entities.User;
import com.example.SchedulerW4.notifications.NotificationMessageProducer;
import com.example.SchedulerW4.repositories.AppointmentRepository;
import com.example.SchedulerW4.repositories.ProviderRepository;
import com.example.SchedulerW4.repositories.SlotRepository;
import com.example.SchedulerW4.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService{

    //Inject Repos
    private final AppointmentRepository appointmentRepository;
    private final SlotRepository slotRepository;
    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;

    //Inject services
    private final SlotService slotService;
    private final UserService userService;
    private final ProviderService providerService;

    //RabbitMQ
    private final NotificationMessageProducer notificationMessageProducer;

    //Fixed variable for maximum number of bookings allowed per user
    private static final int MAX_BOOKINGS_PER_DAY = 2;

    @Override
    @Transactional
    @CacheEvict(value = "adminStats", key = "'dashboardStats'") // Evict the cache
    public AppointmentResponseDto bookAppointment(AppointmentRequestDto dto) {

        User user = userService.findById(dto.getUserId());
        Provider provider = providerService.findById(dto.getProviderId());
        Slot slot = slotService.findById(dto.getSlotId());


        if (slot.isBooked()) {
            throw new RuntimeException("Requested slot is already booked");
        }

        slot.setBooked(true);
        slotRepository.save(slot);

        Appointment appointment = Appointment.builder()
                .user(user)
                .provider(provider)
                .status(Appointment.Status.BOOKED)
                .slot(slot)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Appointment saved = appointmentRepository.save(appointment);

        //RabbitMQ Notif.
        NotificationDto notification = new NotificationDto(
                saved.getUser().getEmail(),
                "Your Appointment is Confirmed!",
                "Your appointment with " + saved.getProvider().getName() +
                        " on " + saved.getSlot().getStartTime() +
                        " is confirmed!"
        );

        notificationMessageProducer.sendNotification(notification);
        return mapToResponse(saved);

    }

    @Override
    public List<AppointmentResponseDto> getAllAppointments(Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User could not be found"));
        return appointmentRepository.findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

    }

    //Get all Provider appointments
    @Override
    public List<AppointmentResponseDto> getProviderAppointments(Long providerId) {
        Provider provider = providerRepository.findById(providerId).orElseThrow(() -> new RuntimeException("Provider could not be found"));

        return appointmentRepository.findByProvider(provider)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    @Override
    @CacheEvict(value = "adminStats", key = "'dashboardStats'") // Evict the cache
    public AppointmentResponseDto rescheduleAppointment(Long appointmentId, Long newSlotId, Long userId) {

        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(() -> new RuntimeException("Appointment Not Found For ID: " + appointmentId));

        Slot newSlot = slotService.findById(newSlotId);

        if (!appointment.getUser().getId().equals(userId)) {
            throw new RuntimeException("User not authorized to reschedule this appointment.");
        }

        if (newSlot.isBooked()) {
            throw new RuntimeException("This slot is already booked, try another");
        }

        Slot oldSlot = appointment.getSlot();

        oldSlot.setBooked(false);
        newSlot.setBooked(true);

        appointment.setSlot(newSlot);
        appointment.setUpdatedAt(LocalDateTime.now());

        Appointment saved = appointmentRepository.save(appointment);

        //RabbitMQ Notif.
        NotificationDto notification = new NotificationDto(
                saved.getUser().getEmail(),
                "Your Appointment is Rescheduled!",
                "Your appointment with " + saved.getProvider().getName() +
                        " on " + saved.getSlot().getStartTime() +
                        " is confirmed!"
        );

        notificationMessageProducer.sendNotification(notification);

        return mapToResponse(appointmentRepository.save(appointment));
    }

    @Override
    @CacheEvict(value = "adminStats", key = "'dashboardStats'") // Evict the cache
    public void cancelAppointment(Long appointmentId, Long userId) {
        // 1. Find the specific appointment
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment Not Found For ID: " + appointmentId));

        // 2. Validate that the appointment belongs to the user attempting to cancel it
        if (!appointment.getUser().getId().equals(userId)) {
            throw new RuntimeException("User not authorized to cancel this appointment.");
        }

        // 3. Check current status to prevent cancelling an already cancelled appointment
        if (appointment.getStatus() == Appointment.Status.CANCELLED) {
            throw new RuntimeException("Appointment is already cancelled");
        }

        // 4. Update the appointment status to CANCELLED
        appointment.setStatus(Appointment.Status.CANCELLED);
        appointment.setUpdatedAt(LocalDateTime.now());

        // 5. Free up the associated slot
        Slot slot = appointment.getSlot();
        if (slot != null) { // Defensive check
            slot.setBooked(false);
            slotRepository.save(slot); // Save the updated slot status
        }

        // 6. Save the updated appointment (DO NOT DELETE!)
        appointmentRepository.save(appointment);

        // The DTO construction and sending logic remain exactly the same
        NotificationDto notification = new NotificationDto(
                appointment.getUser().getEmail(),
                "Your Appointment is Cancelled!",
                "Your appointment with " + appointment.getProvider().getName() +
                        " on " + appointment.getSlot().getStartTime() + " has been cancelled!"
        );
        notificationMessageProducer.sendNotification(notification);
    }

    private AppointmentResponseDto mapToResponse (Appointment appointment) {
        return AppointmentResponseDto.builder()
                .appointmentId(appointment.getId())
                .slotId(appointment.getSlot().getId())
                .userName(appointment.getUser().getName())
                .providerId(appointment.getProvider().getId())
                .providerName(appointment.getProvider().getName())
                .specialization(appointment.getProvider().getSpecialization())
                .startTime(appointment.getSlot().getStartTime())
                .endTime(appointment.getSlot().getStartTime().plusHours(1))
                .status(appointment.getStatus())
                .build();
    }

}
