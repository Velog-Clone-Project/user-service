package com.example.userservice.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserBlogResponse {

    private UserInfoDto user;
    private PostListResponse posts;
}
