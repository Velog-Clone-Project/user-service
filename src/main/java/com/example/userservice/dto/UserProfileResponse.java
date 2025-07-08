package com.example.userservice.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileResponse {

    private String profileName;
    private String profileImageUrl;
    private String bio;
    private String email;
}
