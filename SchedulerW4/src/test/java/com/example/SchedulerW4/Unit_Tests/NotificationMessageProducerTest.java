package com.example.SchedulerW4.Unit_Tests;

import com.example.SchedulerW4.dtos.notification_dtos.NotificationDto;
import com.example.SchedulerW4.notifications.NotificationMessageProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

class NotificationMessageProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private NotificationMessageProducer producer;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendNotification_shouldCallConvertAndSendWithCorrectParams() {
        // Arrange
        NotificationDto dto = new NotificationDto(
                "test@example.com",
                "Test Subject",
                "Test Message"
        );

        // Set private fields via reflection (or use constructor-based @Value injection in your class)
        ReflectionTestUtils.setField(producer, "exchangeName", "test-exchange");
        ReflectionTestUtils.setField(producer, "routingKey", "test-routing");

        // Act
        producer.sendNotification(dto);

        // Assert
        verify(rabbitTemplate, times(1))
                .convertAndSend("test-exchange", "test-routing", dto);
    }
}
