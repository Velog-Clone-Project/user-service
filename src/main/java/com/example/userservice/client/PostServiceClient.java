package com.example.userservice.client;

import com.example.userservice.dto.PostListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "post-service", url = "http://post-service:8003")
public interface PostServiceClient {

    @GetMapping("/internal/posts")
    PostListResponse getPostsByUserId(
            @RequestParam("userId") String userId,
            @RequestParam(value = "cursorId", required = false) Long cursorId
    );

    @GetMapping("/internal/posts/liked")
    PostListResponse getLikedPostsByUserId(
            @RequestParam("userId") String userId,
            @RequestParam(value = "cursorId", required = false) Long cursorId
    );
}
