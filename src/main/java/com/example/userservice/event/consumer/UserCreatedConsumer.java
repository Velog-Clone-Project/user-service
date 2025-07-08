package com.example.userservice.event.consumer;

import com.example.userservice.config.RabbitConfig;
import com.example.userservice.domain.UserEntity;
import com.example.common.event.UserCreatedEvent;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCreatedConsumer {

    private final UserRepository userRepository;

    @RabbitListener(queues = RabbitConfig.USER_CREATED_QUEUE)
    public void handleUserCreated(UserCreatedEvent event) {
        try {
            log.info("[user-service] Received event: {}", event.getUserId());

            UserEntity user = UserEntity.builder()
                    .userId(event.getUserId())
                    .profileName(event.getProfileName())
                    .profileImageUrl("default-profile-image-url") // 기본 프로필 이미지 URL 설정
                    .bio(event.getBio())
                    .createdAt(new Date())
                    .build();

            userRepository.save(user);
        } catch (Exception e) {
            log.error("Failed to process user.created event. Will not retry.\nReason: {}", e.getMessage(), e);
            // Spring에게 재시도하지 말라고 명시
            throw new AmqpRejectAndDontRequeueException("Permanent failure while processing message", e);
        }
    }
}
