package com.example.SchedulerW4.notifications;

import com.example.SchedulerW4.dtos.notification_dtos.NotificationDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationMessageConsumer {

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void receiveNotification (NotificationDto notificationDto) {
        System.out.println("\n--- RabbitMQ Notification Service ---");
        System.out.println("Received Notification for: " + notificationDto.getRecipientEmail());
        System.out.println("Subject: " + notificationDto.getSubject());
        System.out.println("Message: " + notificationDto.getMessage());
        System.out.println("--- End of Notification ---\n");
    }


}
