package com.example.userservice.event;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEventPublisher {

    private final AmqpTemplate amqpTemplate;

    public void sendUserDeletedEvent(String userId) {
        UserDeletedEvent event = new UserDeletedEvent(userId);
        amqpTemplate.convertAndSend("user.exchange", "user.deleted", event);
    }
}
