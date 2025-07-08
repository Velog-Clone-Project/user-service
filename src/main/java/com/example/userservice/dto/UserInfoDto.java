package com.example.userservice.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoDto {

    private String userId;
    private String profileName;
    private String bio;
    private String profileImageUrl;
}
