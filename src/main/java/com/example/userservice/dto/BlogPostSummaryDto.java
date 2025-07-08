package com.example.userservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlogPostSummaryDto {

    private Long postId;
    private String title;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private int commentCount;
    private int likeCount;
}
