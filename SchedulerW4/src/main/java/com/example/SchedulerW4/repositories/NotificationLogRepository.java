package com.example.SchedulerW4.repositories;

import com.example.SchedulerW4.entities.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
}
