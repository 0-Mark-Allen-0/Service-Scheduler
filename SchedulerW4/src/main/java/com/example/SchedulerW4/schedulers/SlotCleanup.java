package com.example.SchedulerW4.schedulers;

import com.example.SchedulerW4.entities.Appointment;
import com.example.SchedulerW4.entities.Slot;
import com.example.SchedulerW4.repositories.AppointmentRepository;
import com.example.SchedulerW4.repositories.SlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlotCleanup {

    private final SlotRepository slotRepository;

    //Appointment Repo:
    private final AppointmentRepository appointmentRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanUp () {

        LocalDateTime now = LocalDateTime.now();


        List<Appointment> cancelled = appointmentRepository.findByStatus(Appointment.Status.CANCELLED);

        log.info("SYSTEM APPOINTMENT CLEANUP - REMOVING " + cancelled.size() + " CANCELLED APPOINTMENTS");
        appointmentRepository.deleteAll(cancelled);


        List<Slot> expired = slotRepository.findByIsBookedFalseAndEndTimeBefore(now);
        log.info("SYSTEM SLOT CLEANUP - REMOVING " + expired.size() + " SLOTS");
        slotRepository.deleteAll(expired);

    }

}
