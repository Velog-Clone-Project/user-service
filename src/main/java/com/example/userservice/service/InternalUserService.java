package com.example.userservice.service;

import com.example.userservice.domain.UserEntity;
import com.example.userservice.dto.InternalUserProfileResponse;
import com.example.userservice.event.UserCreatedEvent;
import com.example.userservice.exception.user.UserNotFoundException;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class InternalUserService {

    private final UserRepository userRepository;

    public void createUser(UserCreatedEvent event) {

        // 이미 존재하면 무시 (idempotent)
        if (userRepository.existsByUserId(event.getUserId())) {
            return;
        }
        UserEntity user = UserEntity.builder()
                .userId(event.getUserId())
                .email(event.getEmail())
                .profileName(event.getProfileName())
                .profileImageUrl("default-profile-image-url")
                .bio(event.getBio())
                .createdAt(new Date())
                .build();

        userRepository.save(user);
    }

    public InternalUserProfileResponse getUserProfileByUserId(String userId) {

        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);

        return InternalUserProfileResponse.builder()
                .profileName(user.getProfileName())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }


}
