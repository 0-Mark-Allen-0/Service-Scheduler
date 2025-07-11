package com.example.SchedulerW4.services;

import com.example.SchedulerW4.dtos.appointment_dtos.AppointmentRequestDto;
import com.example.SchedulerW4.dtos.appointment_dtos.AppointmentResponseDto;
import com.example.SchedulerW4.dtos.appointment_dtos.BookingResponseDto;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final SlotRepository slotRepository;
    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;
    private final SlotService slotService;
    private final UserService userService;
    private final ProviderService providerService;
    private final RedisService redisService;
    private final NotificationMessageProducer notificationMessageProducer;

    private static final int MAX_BOOKINGS_PER_DAY = 2;

    @Override
    @Transactional
    @CacheEvict(value = "adminStats", key = "'dashboardStats'")
    public BookingResponseDto bookAppointment(AppointmentRequestDto dto) {
        User user = userService.findById(dto.getUserId());
        Provider provider = providerService.findById(dto.getProviderId());
        Slot slot = slotService.findById(dto.getSlotId());

        if (!slot.getProvider().getId().equals(provider.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slot does not belong to the specified provider.");
        }

        if (slot.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot book a slot in the past.");
        }

        if (slot.isBooked()) {
            if (redisService.isUserInQueue(slot.getId(), user.getId())) {
                return BookingResponseDto.builder()
                        .status(BookingResponseDto.BookingStatus.ALREADY_QUEUED)
                        .message("You are already in the queue for this slot.")
                        .queuedSlotId(slot.getId())
                        .build();
            } else {
                redisService.addToQueue(slot.getId(), user.getId());

                // CHANGED: Create appointment record with QUEUED status
                Appointment queuedAppointment = Appointment.builder()
                        .user(user)
                        .provider(provider)
                        .slot(slot)
                        .status(Appointment.Status.QUEUED) // NEW: QUEUED status
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                Appointment savedQueuedAppointment = appointmentRepository.save(queuedAppointment);

                // ADD: Debug logging
                System.out.println("=== DEBUG: Queued appointment created ===");
                System.out.println("Appointment ID: " + savedQueuedAppointment.getId());
                System.out.println("User: " + savedQueuedAppointment.getUser().getName());
                System.out.println("Provider: " + savedQueuedAppointment.getProvider().getName());
                System.out.println("Slot ID: " + savedQueuedAppointment.getSlot().getId());
                System.out.println("Status: " + savedQueuedAppointment.getStatus());
                System.out.println("=== END DEBUG ===");

                // Send notification for queued booking
                notificationMessageProducer.sendNotification(new NotificationDto(
                        user.getEmail(),
                        "Added to Waiting List",
                        String.format("You have been added to the waiting list for %s with %s on %s at %s. We will notify you if this slot becomes available.",
                                provider.getSpecialization(), provider.getName(),
                                slot.getStartTime().toLocalDate(), slot.getStartTime().toLocalTime())
                ));

                // Notify provider about queue addition
                notificationMessageProducer.sendNotification(new NotificationDto(
                        provider.getEmail(),
                        "New User in Queue",
                        String.format("User %s has been added to the waiting list for your slot on %s at %s.",
                                user.getName(), slot.getStartTime().toLocalDate(), slot.getStartTime().toLocalTime())
                ));

                return BookingResponseDto.builder()
                        .status(BookingResponseDto.BookingStatus.QUEUED)
                        .message("Slot is currently booked. You have been added to the waiting list for this slot. We will notify you if it becomes available.")
                        .appointment(mapToResponse(savedQueuedAppointment)) // CHANGED: Return the queued appointment
                        .queuedSlotId(slot.getId())
                        .build();
            }
        } else {
            Appointment appointment = Appointment.builder()
                    .user(user)
                    .provider(provider)
                    .slot(slot)
                    .status(Appointment.Status.BOOKED)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            slot.setBooked(true);
            slotRepository.save(slot);
            Appointment savedAppointment = appointmentRepository.save(appointment);

            notificationMessageProducer.sendNotification(new NotificationDto(
                    user.getEmail(),
                    "Appointment Confirmed!",
                    String.format("Your appointment with %s for %s has been successfully booked on %s at %s.",
                            provider.getName(), provider.getSpecialization(),
                            savedAppointment.getSlot().getStartTime().toLocalDate(),
                            savedAppointment.getSlot().getStartTime().toLocalTime())
            ));

            notificationMessageProducer.sendNotification(new NotificationDto(
                    provider.getEmail(),
                    "New Appointment Booked!",
                    String.format("You have a new appointment with %s on %s at %s.",
                            user.getName(), savedAppointment.getSlot().getStartTime().toLocalDate(),
                            savedAppointment.getSlot().getStartTime().toLocalTime())
            ));

            return BookingResponseDto.builder()
                    .status(BookingResponseDto.BookingStatus.BOOKED)
                    .message("Appointment successfully booked!")
                    .appointment(mapToResponse(savedAppointment))
                    .build();
        }
    }

    @Override
    public List<AppointmentResponseDto> getAllAppointments(Long userId) {
        User user = userService.findById(userId);
        List<Appointment> appointments = appointmentRepository.findByUser(user);

        // ADD: Debug logging
        System.out.println("=== DEBUG: getAllAppointments for user ID: " + userId + " ===");
        System.out.println("Found " + appointments.size() + " appointments");
        for (Appointment apt : appointments) {
            System.out.println("Appointment ID: " + apt.getId() +
                    ", Status: " + apt.getStatus() +
                    ", Provider: " + apt.getProvider().getName() +
                    ", Slot ID: " + apt.getSlot().getId());
        }
        System.out.println("=== END DEBUG ===");

        return appointments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentResponseDto> getProviderAppointments(Long providerId) {
        Provider provider = providerService.findById(providerId);
        return appointmentRepository.findByProvider(provider)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "adminStats", key = "'dashboardStats'")
    public BookingResponseDto rescheduleAppointment(Long appointmentId, Long newSlotId, Long userId) {
        Appointment existingAppointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment Not Found For ID: " + appointmentId));

        Slot newSlot = slotService.findById(newSlotId);
        User user = userService.findById(userId);

        if (!existingAppointment.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not authorized to reschedule this appointment.");
        }

        if (newSlot.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot reschedule to a slot in the past.");
        }

        if (!newSlot.getProvider().getId().equals(existingAppointment.getProvider().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New slot must be with the same provider.");
        }

        if (existingAppointment.getSlot().getId().equals(newSlot.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot reschedule to the same slot.");
        }

        Slot oldSlot = existingAppointment.getSlot();
        redisService.removeUserFromQueue(oldSlot.getId(), existingAppointment.getUser().getId());
        handleSlotAvailability(oldSlot.getId());

        if (newSlot.isBooked()) {
            if (redisService.isUserInQueue(newSlot.getId(), existingAppointment.getUser().getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already in the queue for the new slot.");
            }

            redisService.addToQueue(newSlot.getId(), existingAppointment.getUser().getId());
            existingAppointment.setStatus(Appointment.Status.QUEUED); // CHANGED: Set to QUEUED instead of CANCELLED
            existingAppointment.setSlot(newSlot); // CHANGED: Update to new slot
            existingAppointment.setUpdatedAt(LocalDateTime.now());
            Appointment updatedAppointment = appointmentRepository.save(existingAppointment);

            // Send notification for reschedule queue
            notificationMessageProducer.sendNotification(new NotificationDto(
                    user.getEmail(),
                    "Reschedule Request - Added to Queue",
                    String.format("Your request to reschedule to %s is pending. You have been added to the waiting list for the new slot on %s at %s. We will notify you if it becomes available.",
                            newSlot.getProvider().getName(), newSlot.getStartTime().toLocalDate(), newSlot.getStartTime().toLocalTime())
            ));

            notificationMessageProducer.sendNotification(new NotificationDto(
                    existingAppointment.getProvider().getEmail(),
                    "Appointment Reschedule Update",
                    String.format("Appointment for %s (originally for %s at %s) has been rescheduled. User %s is now queued for your slot on %s at %s.",
                            existingAppointment.getUser().getName(), oldSlot.getStartTime().toLocalDate(), oldSlot.getStartTime().toLocalTime(),
                            user.getName(), newSlot.getStartTime().toLocalDate(), newSlot.getStartTime().toLocalTime())
            ));

            return BookingResponseDto.builder()
                    .status(BookingResponseDto.BookingStatus.QUEUED)
                    .message("The new slot is currently booked. You have been added to the waiting list for it.")
                    .appointment(mapToResponse(updatedAppointment)) // CHANGED: Return the updated appointment
                    .queuedSlotId(newSlot.getId())
                    .build();
        } else {
            existingAppointment.setSlot(newSlot);
            existingAppointment.setStatus(Appointment.Status.BOOKED);
            existingAppointment.setUpdatedAt(LocalDateTime.now());
            Appointment updatedAppointment = appointmentRepository.save(existingAppointment);

            newSlot.setBooked(true);
            slotRepository.save(newSlot);

            notificationMessageProducer.sendNotification(new NotificationDto(
                    user.getEmail(),
                    "Appointment Rescheduled!",
                    String.format("Your appointment with %s has been successfully rescheduled from %s at %s to %s at %s.",
                            existingAppointment.getProvider().getName(), oldSlot.getStartTime().toLocalDate(), oldSlot.getStartTime().toLocalTime(),
                            newSlot.getStartTime().toLocalDate(), newSlot.getStartTime().toLocalTime())
            ));

            notificationMessageProducer.sendNotification(new NotificationDto(
                    existingAppointment.getProvider().getEmail(),
                    "Appointment Rescheduled Update",
                    String.format("Appointment for %s has been rescheduled from %s at %s to %s at %s.",
                            user.getName(), oldSlot.getStartTime().toLocalDate(), oldSlot.getStartTime().toLocalTime(),
                            newSlot.getStartTime().toLocalDate(), newSlot.getStartTime().toLocalTime())
            ));

            return BookingResponseDto.builder()
                    .status(BookingResponseDto.BookingStatus.BOOKED)
                    .message("Appointment successfully rescheduled!")
                    .appointment(mapToResponse(updatedAppointment))
                    .build();
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "adminStats", key = "'dashboardStats'")
    public void cancelAppointment(Long appointmentId, Long userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment Not Found For ID: " + appointmentId));

        if (!appointment.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not authorized to cancel this appointment.");
        }

        if (appointment.getStatus() == Appointment.Status.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment is already cancelled.");
        }

        Slot cancelledSlot = appointment.getSlot();

        // CHANGED: Remove from queue if appointment was queued
        if (appointment.getStatus() == Appointment.Status.QUEUED) {
            redisService.removeUserFromQueue(cancelledSlot.getId(), appointment.getUser().getId());
        }

        appointment.setStatus(Appointment.Status.CANCELLED);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);

        notificationMessageProducer.sendNotification(new NotificationDto(
                appointment.getUser().getEmail(),
                "Appointment Cancellation Confirmation",
                String.format("Your appointment with %s on %s at %s has been cancelled.",
                        appointment.getProvider().getName(), cancelledSlot.getStartTime().toLocalDate(), cancelledSlot.getStartTime().toLocalTime())
        ));

        // Only handle slot availability if the appointment was actually booked (not queued)
        if (appointment.getStatus() != Appointment.Status.QUEUED) {
            handleSlotAvailability(cancelledSlot.getId());
        }

        Slot currentSlotState = slotRepository.findById(cancelledSlot.getId()).orElse(null);
        if (currentSlotState != null && !currentSlotState.isBooked() && redisService.getQueueSize(cancelledSlot.getId()) == 0) {
            notificationMessageProducer.sendNotification(new NotificationDto(
                    appointment.getProvider().getEmail(),
                    "Slot is Now Open",
                    String.format("Your slot on %s at %s (originally booked by %s) is now open and available for new bookings.",
                            cancelledSlot.getStartTime().toLocalDate(), cancelledSlot.getStartTime().toLocalTime(), appointment.getUser().getName())
            ));
        }
    }

    @Transactional
    private void handleSlotAvailability(Long slotId) {
        Slot slot = slotService.findById(slotId);
        Long nextUserIdInQueue = redisService.popFromQueue(slotId);

        if (nextUserIdInQueue != null) {
            User nextUser = userRepository.findById(nextUserIdInQueue)
                    .orElseGet(() -> {
                        System.err.println("User with ID " + nextUserIdInQueue + " from Redis queue not found for slot " + slotId + ". Trying next in queue.");
                        return null;
                    });

            if (nextUser != null) {
                // CHANGED: Find and update existing queued appointment instead of creating new one
                List<Appointment> queuedAppointments = appointmentRepository.findByUserAndSlotAndStatus(
                        nextUser, slot, Appointment.Status.QUEUED);

                if (!queuedAppointments.isEmpty()) {
                    Appointment queuedAppointment = queuedAppointments.get(0);
                    queuedAppointment.setStatus(Appointment.Status.BOOKED);
                    queuedAppointment.setUpdatedAt(LocalDateTime.now());
                    appointmentRepository.save(queuedAppointment);
                } else {
                    // Fallback: create new appointment if queued one not found
                    Appointment newAppointment = Appointment.builder()
                            .user(nextUser)
                            .provider(slot.getProvider())
                            .slot(slot)
                            .status(Appointment.Status.BOOKED)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    appointmentRepository.save(newAppointment);
                }

                slot.setBooked(true);
                slotRepository.save(slot);

                notificationMessageProducer.sendNotification(new NotificationDto(
                        nextUser.getEmail(),
                        "Your Appointment is Confirmed!",
                        String.format("Great news! Your waiting slot for %s with %s on %s at %s is now confirmed! You are officially booked.",
                                slot.getProvider().getName(), slot.getProvider().getSpecialization(),
                                slot.getStartTime().toLocalDate(), slot.getStartTime().toLocalTime())
                ));

                notificationMessageProducer.sendNotification(new NotificationDto(
                        slot.getProvider().getEmail(),
                        "Slot Rebooked Automatically!",
                        String.format("Your slot on %s at %s has been automatically rebooked by %s (from the waiting list).",
                                slot.getStartTime().toLocalDate(), slot.getStartTime().toLocalTime(), nextUser.getName())
                ));
            } else {
                handleSlotAvailability(slotId);
            }
        } else {
            slot.setBooked(false);
            slotRepository.save(slot);
        }
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
