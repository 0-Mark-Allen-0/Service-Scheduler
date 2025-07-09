package com.example.SchedulerW4.dtos.notification_dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    private String recipientEmail;

    private String subject;

    private String message;

}
