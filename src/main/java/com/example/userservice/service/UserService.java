package com.example.userservice.service;

import com.example.userservice.domain.UserEntity;
import com.example.userservice.dto.*;
import com.example.userservice.event.UserEventPublisher;
import com.example.userservice.exception.InvalidFileTypeException;
import com.example.userservice.exception.NoImageProvidedException;
import com.example.userservice.exception.UserAccessDeniedException;
import com.example.userservice.exception.user.InvalidCursorIdException;
import com.example.userservice.exception.user.UserNotFoundException;
import com.example.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MinioService minioService;
    private final UserEventPublisher userEventPublisher;

    private static final String DEFAULT_PROFILE_IMAGE_URL = "https://default-profile-image-url";

    public UserBlogResponse getUserBlog(String userId, Long cursorId) {

        if (cursorId != null && cursorId <= 0) {
            throw new InvalidCursorIdException();
        }

        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);

        // TODO: post-service 연동 예정, 현재는 mock data 사용
        List<BlogPostSummaryDto> mockPosts = List.of(
                // 1번째 포스트
                BlogPostSummaryDto.builder()
                        .postId(123L)
                        .title("MSA 아키텍처 정리")
                        .thumbnailUrl("https://thumbnail-image-url")
                        .createdAt(LocalDateTime.of(2025, 3, 28, 11, 0))
                        .commentCount(5)
                        .likeCount(5)
                        .build(),
                // 2번째 포스트
                BlogPostSummaryDto.builder()
                        .postId(122L)
                        .title("Docker vs Kubernetes")
                        .thumbnailUrl("https://thumbnail-image-url")
                        .createdAt(LocalDateTime.of(2025, 3, 25, 9, 20))
                        .commentCount(3)
                        .likeCount(10)
                        .build()
        );

        return UserBlogResponse.builder()
                .user(UserInfoDto.builder()
                        .userId(user.getUserId())
                        .profileName(user.getProfileName())
                        .bio(user.getBio())
                        .profileImageUrl(user.getProfileImageUrl())
                        .build())
                .posts(mockPosts)
                .nextCursorId(122L)
                .hasNext(true)
                .build();
    }

    public UserProfileResponse getMyProfile(String userId) {

        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);

        if(!user.getUserId().equals(userId)) {
            throw  new UserAccessDeniedException();
        }

        return UserProfileResponse.builder()
                .profileName(user.getProfileName())
                .profileImageUrl(user.getProfileImageUrl())
                .bio(user.getBio())
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {

        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);

        if(!user.getUserId().equals(userId)) {
            throw  new UserAccessDeniedException();
        }

        if (request.getProfileName() != null) {
            user.setProfileName(request.getProfileName());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        // TODO: 이 변경사항을 post-service로 동기화하기 위한 이벤트 전송 필요 (ex. RabbitMQ)

        return UserProfileResponse.builder()
                .profileName(user.getProfileName())
                .bio(user.getBio())
                .profileImageUrl(user.getProfileImageUrl())
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public UserProfileResponse updateProfileImage(String userId, MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new NoImageProvidedException();
        }

        String contentType = file.getContentType();
        if (!List.of("image/jpeg", "image/png", "image/jpg").contains(contentType)) {
            throw new InvalidFileTypeException();
        }

        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);

        if(!user.getUserId().equals(userId)) {
            throw  new UserAccessDeniedException();
        }

        String oldImageUrl = user.getProfileImageUrl();
        if (!DEFAULT_PROFILE_IMAGE_URL.equals(oldImageUrl)) {
            minioService.deleteIfUploaded(oldImageUrl);
        }

        String originalFilename = file.getOriginalFilename();
        String objectName = UUID.randomUUID() + "-" + originalFilename;

        String imageUrl = minioService.upload(file, objectName);

        user.setProfileImageUrl(imageUrl);

        // TODO: 이 변경사항을 post-service로 동기화하기 위한 이벤트 전송 필요 (ex. RabbitMQ)

        return UserProfileResponse.builder()
                .profileName(user.getProfileName())
                .bio(user.getBio())
                .profileImageUrl(user.getProfileImageUrl())
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public UserProfileResponse deleteProfileImage(String userId) {

        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);

        if(!user.getUserId().equals(userId)) {
            throw  new UserAccessDeniedException();
        }

        String oldImageUrl = user.getProfileImageUrl();
        if (!DEFAULT_PROFILE_IMAGE_URL.equals(oldImageUrl)) {
            minioService.deleteIfUploaded(oldImageUrl);
        }

        user.setProfileImageUrl(DEFAULT_PROFILE_IMAGE_URL);

        // TODO: 이 변경사항을 post-service로 동기화하기 위한 이벤트 전송 필요 (ex. RabbitMQ)

        return UserProfileResponse.builder()
                .profileName(user.getProfileName())
                .bio(user.getBio())
                .profileImageUrl(user.getProfileImageUrl())
                .email(user.getEmail())
                .build();
    }

    public PostListResponse getLikedPosts(String userId, Long cursorId) {
        // TODO: Post-service 와 연동하여 실제 좋아요한 게시글을 가져오는 로직 구현 필요 (ex. FeignClient 또는 RabbitMQ)

        List<PostSummaryDto> mockPosts = List.of(
                PostSummaryDto.builder()
                        .postId(123L)
                        .title("MSA 아키텍처 정리")
                        .thumbnailUrl("https://cdn.localhost/thumbnails/post123.jpg")
                        .authorName("backendking")
                        .authorProfileImageUrl("https://cdn.localhost/profile/jihyun01.jpg")
                        .createdAt(LocalDateTime.of(2025, 3, 28, 11, 0))
                        .commentCount(5)
                        .likeCount(5)
                        .build(),
                PostSummaryDto.builder()
                        .postId(122L)
                        .title("Docker vs Kubernetes")
                        .thumbnailUrl("https://cdn.localhost/thumbnails/post122.jpg")
                        .authorName("jihyunDev")
                        .authorProfileImageUrl("https://cdn.localhost/profile/jihyun02.jpg")
                        .createdAt(LocalDateTime.of(2025, 3, 25, 9, 20))
                        .commentCount(3)
                        .likeCount(10)
                        .build()
        );

        return PostListResponse.builder()
                .posts(mockPosts)
                .nextCursorId(122L)
                .hasNext(true)
                .build();
    }

    public void deleteUser(String userId) {

        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);

        if(!user.getUserId().equals(userId)) {
            throw  new UserAccessDeniedException();
        }

        userRepository.delete(user);

        // TODO: 작성한 게시글, 댓글, 좋아요 등 관련 데이터도 post-service, comment-service 등에서 함께 삭제되도록 이벤트 발행 (RabbitMQ)
        userEventPublisher.sendUserDeletedEvent(userId);
    }
}
