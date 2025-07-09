package com.example.SchedulerW4.repositories;

import com.example.SchedulerW4.entities.Appointment;
import com.example.SchedulerW4.entities.Provider;
import com.example.SchedulerW4.entities.Slot;
import com.example.SchedulerW4.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByUser (User user);

    List<Appointment> findByProvider (Provider provider);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.provider JOIN FETCH a.slot")
    List<Appointment> findAllWithProviderAndSlot ();

    //For deleted appointment scheduled cleanup
    List<Appointment> findByStatusAndSlotEndTimeBefore(Appointment.Status status, LocalDateTime slotEndTime);

    List<Appointment> findByStatus(Appointment.Status status);

    List<Appointment> findBySlot(Slot slot);
}
