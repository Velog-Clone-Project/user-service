package com.example.userservice.controller;

import com.example.userservice.event.UserCreatedEvent;
import com.example.userservice.service.InternalUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
