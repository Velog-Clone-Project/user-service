package com.example.userservice.event;

import com.example.userservice.config.RabbitProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEventPublisher {

    private final AmqpTemplate amqpTemplate;
    private final RabbitProperties properties;

    public void sendUserDeletedEvent(String userId) {
        amqpTemplate.convertAndSend(
                properties.getExchanges().getUser(),
                properties.getRoutingKeys().getUser().getCreated(),
                new UserDeletedEvent(userId)
        );
    }

    public void sendUserUpdatedEvent(UpdateAuthorInfoEvent event) {
        amqpTemplate.convertAndSend(
                properties.getExchanges().getUser(),
                properties.getRoutingKeys().getUser().getUpdated(),
                event
        );
    }
}
