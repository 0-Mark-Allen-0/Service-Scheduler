package com.example.SchedulerW4.Unit_Tests;

import com.example.SchedulerW4.dtos.appointment_dtos.AppointmentRequestDto;
import com.example.SchedulerW4.dtos.notification_dtos.NotificationDto;
import com.example.SchedulerW4.entities.*;
import com.example.SchedulerW4.notifications.NotificationMessageProducer;
import com.example.SchedulerW4.repositories.*;
import com.example.SchedulerW4.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppointmentServiceImplTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private SlotRepository slotRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProviderRepository providerRepository;
    @Mock private SlotService slotService;
    @Mock private UserService userService;
    @Mock private ProviderService providerService;
    @Mock private NotificationMessageProducer notificationProducer;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private User user;
    private Provider provider;
    private Slot slot;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = User.builder().id(1L).name("John").email("john@example.com").build();
        provider = Provider.builder().id(2L).name("Dr. Who").specialization("Neurology").build();
        slot = Slot.builder().id(3L).startTime(LocalDateTime.now().plusDays(1)).isBooked(false).provider(provider).build();
        appointment = Appointment.builder()
                .id(10L).user(user).provider(provider).slot(slot)
                .status(Appointment.Status.BOOKED)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void bookAppointment_successfullyBooks() {
        AppointmentRequestDto dto = new AppointmentRequestDto(user.getId(), provider.getId(), slot.getId());

        when(userService.findById(user.getId())).thenReturn(user);
        when(providerService.findById(provider.getId())).thenReturn(provider);
        when(slotService.findById(slot.getId())).thenReturn(slot);
        when(slotRepository.save(slot)).thenReturn(slot);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        var response = appointmentService.bookAppointment(dto);

        assertThat(response.getAppointmentId()).isEqualTo(appointment.getId());
        verify(notificationProducer).sendNotification(any(NotificationDto.class));
    }

    @Test
    void bookAppointment_failsIfSlotAlreadyBooked() {
        slot.setBooked(true);
        AppointmentRequestDto dto = new AppointmentRequestDto(user.getId(), provider.getId(), slot.getId());

        when(slotService.findById(slot.getId())).thenReturn(slot);

        assertThatThrownBy(() -> appointmentService.bookAppointment(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Requested slot is already booked");
    }

    @Test
    void cancelAppointment_success() {
        appointment.setSlot(slot);

        when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));
        when(slotRepository.save(slot)).thenReturn(slot);
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        appointmentService.cancelAppointment(appointment.getId(), user.getId());

        assertThat(appointment.getStatus()).isEqualTo(Appointment.Status.CANCELLED);
        verify(notificationProducer).sendNotification(any(NotificationDto.class));
    }

    @Test
    void cancelAppointment_unauthorizedUser_throwsException() {
        User otherUser = User.builder().id(999L).build();
        appointment.setUser(otherUser);

        when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(appointment.getId(), user.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not authorized");
    }

    @Test
    void cancelAppointment_alreadyCancelled_throwsException() {
        appointment.setStatus(Appointment.Status.CANCELLED);
        when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(appointment.getId(), user.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already cancelled");
    }

    @Test
    void getAllAppointments_returnsAppointmentsForUser() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(appointmentRepository.findByUser(user)).thenReturn(List.of(appointment));

        var result = appointmentService.getAllAppointments(user.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserName()).isEqualTo(user.getName());
    }

    @Test
    void getProviderAppointments_returnsAppointments() {
        when(providerRepository.findById(provider.getId())).thenReturn(Optional.of(provider));
        when(appointmentRepository.findByProvider(provider)).thenReturn(List.of(appointment));

        var result = appointmentService.getProviderAppointments(provider.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProviderName()).isEqualTo(provider.getName());
    }

    @Test
    void rescheduleAppointment_success() {
        Slot newSlot = Slot.builder().id(9L).startTime(LocalDateTime.now().plusDays(2)).isBooked(false).build();
        appointment.setUser(user);

        when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));
        when(slotService.findById(newSlot.getId())).thenReturn(newSlot);
        when(appointmentRepository.save(any())).thenReturn(appointment);

        var response = appointmentService.rescheduleAppointment(appointment.getId(), newSlot.getId(), user.getId());
        assertThat(appointment.getSlot().getId()).isEqualTo(newSlot.getId());
        assertThat(response.getSlotId()).isEqualTo(newSlot.getId());
    }

    @Test
    void rescheduleAppointment_withBookedSlot_throws() {
        Slot newSlot = Slot.builder().id(9L).startTime(LocalDateTime.now().plusDays(2)).isBooked(true).build();

        when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));
        when(slotService.findById(newSlot.getId())).thenReturn(newSlot);

        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(appointment.getId(), newSlot.getId(), user.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already booked");
    }
}
