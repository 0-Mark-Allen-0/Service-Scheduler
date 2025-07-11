package com.example.SchedulerW4.repositories;

import com.example.SchedulerW4.entities.Appointment;
import com.example.SchedulerW4.entities.Provider;
import com.example.SchedulerW4.entities.User;
import com.example.SchedulerW4.entities.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByUser(User user);
    List<Appointment> findByProvider(Provider provider);

    // NEW: Method to find appointments by user, slot, and status
    List<Appointment> findByUserAndSlotAndStatus(User user, Slot slot, Appointment.Status status);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.provider JOIN FETCH a.slot")
    List<Appointment> findAllWithProviderAndSlot();

    List<Appointment> findByStatus(Appointment.Status status);
}
