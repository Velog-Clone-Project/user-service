package com.example.userservice.controller;

import com.example.userservice.dto.InternalUserProfileResponse;
import com.example.userservice.event.UserCreatedEvent;
import com.example.userservice.service.InternalUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final InternalUserService internalUserService;

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserCreatedEvent event) {
        internalUserService.createUser(event);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<?> getUserProfile(@PathVariable String userId) {

        InternalUserProfileResponse response = internalUserService.getUserProfileByUserId(userId);

        return ResponseEntity.ok(response);
    }
}
