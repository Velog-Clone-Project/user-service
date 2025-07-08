package com.example.userservice.controller;

import com.example.common.dto.ApiResponse;
import com.example.userservice.dto.PostListResponse;
import com.example.userservice.dto.UpdateProfileRequest;
import com.example.userservice.dto.UserBlogResponse;
import com.example.userservice.dto.UserProfileResponse;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}/blog")
    public ResponseEntity<ApiResponse<UserBlogResponse>> getUserBlog(
            @PathVariable String userId,
            @RequestParam(required = false) Long cursorId) {

        UserBlogResponse response = userService.getUserBlog(userId, cursorId);

        return ResponseEntity
                .ok(new ApiResponse<>("User blog loaded", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @RequestHeader("X-User-Id") String userId) {

        UserProfileResponse response = userService.getMyProfile(userId);

        return ResponseEntity
                .ok(new ApiResponse<>("User Profile loaded", response));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody UpdateProfileRequest request) {

        UserProfileResponse response = userService.updateProfile(userId, request);

        return ResponseEntity
                .ok(new ApiResponse<>("User profile updated", response));
    }

    @PatchMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfileImage(
            @RequestHeader("X-User-Id") String userId,
            @RequestPart("image") MultipartFile image) {

        UserProfileResponse response = userService.updateProfileImage(userId, image);

        return ResponseEntity
                .ok(new ApiResponse<>("Profile image uploaded", response));
    }

    @DeleteMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<UserProfileResponse>> deleteProfileImage(
            @RequestHeader("X-User-Id") String userId) {

        UserProfileResponse response = userService.deleteProfileImage(userId);

        return ResponseEntity
                .ok(new ApiResponse<>("Profile image reset to default", response));
    }

    @GetMapping("/me/liked-posts")
    public ResponseEntity<ApiResponse<PostListResponse>> getLikedPosts(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(value = "cursorId", required = false) Long cursorId) {

        PostListResponse response = userService.getLikedPosts(userId, cursorId);

        return ResponseEntity
                .ok(new ApiResponse<>("Liked posts retrieved", response));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Map> deleteUser(@RequestHeader("X-User-Id") String userId) {

        userService.deleteUser(userId);

        return ResponseEntity
                .ok(Map.of("message", "Account deletion completed successfully"));
    }
}
