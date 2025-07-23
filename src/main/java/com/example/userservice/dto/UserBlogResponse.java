package com.example.userservice.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserBlogResponse {

    private UserInfoDto user;
    private List<PostSummaryDto> posts;
    private Long nextCursorId;
    private boolean hasNext;
}
