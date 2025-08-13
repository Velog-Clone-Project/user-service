package com.example.userservice.service;

import com.example.userservice.client.PostServiceClient;
import com.example.userservice.domain.UserEntity;
import com.example.userservice.dto.*;
import com.example.userservice.event.UpdateAuthorInfoEvent;
import com.example.userservice.event.UserEventPublisher;
import com.example.userservice.exception.*;
import com.example.userservice.exception.user.InvalidCursorIdException;
import com.example.userservice.exception.user.UserNotFoundException;
import com.example.userservice.repository.UserRepository;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MinioService minioService;
    private final UserEventPublisher userEventPublisher;

    private final PostServiceClient postServiceClient;

    private static final String DEFAULT_PROFILE_IMAGE_URL = "http://192.168.192.2:9000/images/default-image.png";

    public UserBlogResponse getUserBlog(String userId, Long cursorId) {

        if (cursorId != null && cursorId <= 0) {
            throw new InvalidCursorIdException();
        }

        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);

        PostSummaryListDto posts;
        try {
            posts = postServiceClient.getPostsByUserId(userId, cursorId);
        } catch (FeignException.NotFound e) {
            throw new PostNotFoundException();
        } catch (FeignException e) {
            throw new ExternalServiceException(e);
        }

        return UserBlogResponse.builder()
                .user(UserInfoDto.builder()
                        .userId(user.getUserId())
                        .profileName(user.getProfileName())
                        .bio(user.getBio())
                        .profileImageUrl(user.getProfileImageUrl())
                        .build())
                .posts(posts)
                .build();
    }

    public UserProfileResponse getMyProfile(String userId) {

        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);

        if (!user.getUserId().equals(userId)) {
            throw new UserAccessDeniedException();
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

        if (!user.getUserId().equals(userId)) {
            throw new UserAccessDeniedException();
        }

        if (request.getProfileName() != null) {
            user.setProfileName(request.getProfileName());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        UpdateAuthorInfoEvent event = UpdateAuthorInfoEvent.builder()
                .userId(user.getUserId())
                .authorName(user.getProfileName())
                .authorProfileImageUrl(user.getProfileImageUrl())
                .build();

        userEventPublisher.sendUserUpdatedEvent(event);

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

        if (!user.getUserId().equals(userId)) {
            throw new UserAccessDeniedException();
        }

        String oldImageUrl = user.getProfileImageUrl();
        if (!DEFAULT_PROFILE_IMAGE_URL.equals(oldImageUrl)) {
            minioService.deleteIfUploaded(oldImageUrl);
        }

        String imageUrl = minioService.upload(file);

        user.setProfileImageUrl(imageUrl);

        UpdateAuthorInfoEvent event = UpdateAuthorInfoEvent.builder()
                .userId(user.getUserId())
                .authorName(user.getProfileName())
                .authorProfileImageUrl(user.getProfileImageUrl())
                .build();

        userEventPublisher.sendUserUpdatedEvent(event);

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

        if (!user.getUserId().equals(userId)) {
            throw new UserAccessDeniedException();
        }

        String oldImageUrl = user.getProfileImageUrl();
        if (!DEFAULT_PROFILE_IMAGE_URL.equals(oldImageUrl)) {
            minioService.deleteIfUploaded(oldImageUrl);
        }

        user.setProfileImageUrl(DEFAULT_PROFILE_IMAGE_URL);

        UpdateAuthorInfoEvent event = UpdateAuthorInfoEvent.builder()
                .userId(user.getUserId())
                .authorName(user.getProfileName())
                .authorProfileImageUrl(user.getProfileImageUrl())
                .build();

        userEventPublisher.sendUserUpdatedEvent(event);

        return UserProfileResponse.builder()
                .profileName(user.getProfileName())
                .bio(user.getBio())
                .profileImageUrl(user.getProfileImageUrl())
                .email(user.getEmail())
                .build();
    }

    public PostSummaryListDto getLikedPosts(String userId, Long cursorId) {

        if (cursorId != null && cursorId <= 0) {
            throw new InvalidCursorIdException();
        }

        try {
            return postServiceClient.getLikedPostsByUserId(userId, cursorId);
        } catch (FeignException.NotFound e) {
            throw new PostNotFoundException();
        } catch (FeignException e) {
            throw new ExternalServiceException(e);
        }
    }

    public void deleteUser(String userId) {

        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);

        if (!user.getUserId().equals(userId)) {
            throw new UserAccessDeniedException();
        }

        userRepository.delete(user);

        userEventPublisher.sendUserDeletedEvent(userId);
    }
}
