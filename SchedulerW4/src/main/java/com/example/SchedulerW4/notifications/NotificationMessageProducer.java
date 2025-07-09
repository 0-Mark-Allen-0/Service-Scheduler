package com.example.SchedulerW4.notifications;

import com.example.SchedulerW4.dtos.notification_dtos.NotificationDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    public NotificationMessageProducer (RabbitTemplate rabbitTemplate) {
       this.rabbitTemplate = rabbitTemplate;
    }

    public void sendNotification (NotificationDto notificationDto) {
        System.out.println("Sending message (RabbitMQ): " + notificationDto);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, notificationDto);
    }


}
