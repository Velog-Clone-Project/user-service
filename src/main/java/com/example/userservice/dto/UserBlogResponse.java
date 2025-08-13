package com.example.userservice.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserBlogResponse {

    private UserInfoDto user;
    private PostSummaryListDto posts;
}
