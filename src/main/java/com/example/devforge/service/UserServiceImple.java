package com.example.devforge.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.devforge.dto.UserCreateRequestDto;
import com.example.devforge.dto.UserResponseDto;
import com.example.devforge.dto.UserSummaryDto;
import com.example.devforge.dto.UserUpdateDto;
import com.example.devforge.entity.User;
import com.example.devforge.exception.BadRequestException;
import com.example.devforge.exception.ConflictException;
import com.example.devforge.exception.ResourceNotFoundException;
import com.example.devforge.repository.FollowRepository;
import com.example.devforge.repository.UserRepository;
import com.example.devforge.security.AuthUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional

public class UserServiceImple implements UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthUtil authUtil;

    @Override
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @CacheEvict(cacheNames = "userPages", allEntries = true)
    public UserResponseDto createUser(UserCreateRequestDto dto) {

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ConflictException("User already exists");
        }

        if (userRepository.findByUserName(dto.getUserName()).isPresent()) {
            throw new ConflictException("Username already taken");
        }

        LocalDate today = LocalDate.now();
        if (dto.getDateOfBirth() != null
                && (dto.getDateOfBirth().isAfter(today) || dto.getDateOfBirth().isEqual(today))) {
            throw new BadRequestException("Your date of birth is invalid");
        }

        User user = User.builder()
                .email(dto.getEmail())
                .userName(dto.getUserName())
                .isPrivate(Boolean.TRUE.equals(dto.getIsPrivate()))
                .build();
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setProfilePictureUrl(dto.getProfilePictureUrl());
        user.setDateOfBirth(dto.getDateOfBirth());
        user.setBio(dto.getBio());
        user.setLocation(dto.getLocation());
        if (dto.getSkills() != null) {
            user.setSkills(dto.getSkills());
        }
        if (dto.getInterests() != null) {
            user.setInterests(dto.getInterests());
        }

        User savedUser = userRepository.save(user);

        return toUserResponse(savedUser);
    }

    @Override
    @Cacheable(cacheNames = "userProfiles", key = "#userId + ':viewer:' + @authUtil.getCurrentCacheUserId()")
    public UserResponseDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this id : {}" + userId));

        requireCanViewUser(user);

        UserResponseDto response = toUserResponse(user);
        Long followerCount = followRepository.countByFollowingId(userId);
        Long followingCount = followRepository.countByFollowerId(userId);

        response.setFollowerCount(followerCount);
        response.setFollowingCount(followingCount);

        return response;
    }

    @Override
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @CachePut(cacheNames = "userProfiles", key = "#userId + ':viewer:' + @authUtil.getCurrentCacheUserId()")
    @CacheEvict(cacheNames = "userPages", allEntries = true)
    public UserResponseDto updateUser(Long userId, UserUpdateDto dto) {
        authUtil.requireCurrentUserOrAdmin(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (dto.getUserName() != null && !dto.getUserName().equals(user.getUserName())
                && userRepository.findByUserName(dto.getUserName()).isPresent()) {
            throw new ConflictException("Username already taken");
        }

        if (dto.getBio() != null) {
            user.setBio(dto.getBio());
        }
        if (dto.getDateOfBirth() != null) {
            user.setDateOfBirth(dto.getDateOfBirth());
        }
        if (dto.getUserName() != null) {
            user.setUserName(dto.getUserName());
        }
        if (dto.getLocation() != null) {
            user.setLocation(dto.getLocation());
        }
        if (dto.getSkills() != null) {
            user.setSkills(dto.getSkills());
        }
        if (dto.getInterests() != null) {
            user.setInterests(dto.getInterests());
        }
        if (dto.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(dto.getProfilePictureUrl());
        }

        User updatedUser = userRepository.save(user);

        return toUserResponse(updatedUser);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @CacheEvict(cacheNames = {"userProfiles", "userPages", "projectPages", "feedPages"}, allEntries = true)
    public void deleteUser(Long userId) {
        authUtil.requireCurrentUserOrAdmin(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this id : {}" + userId));
        userRepository.delete(user);

    }

    @Override
    public Page<UserSummaryDto> getAllUsers(int page, int size, String sortBy, String direction) {

        Pageable pageable = buildPageRequest(page, size, sortBy, direction);
        Page<User> usersPage = userRepository.findVisibleUsers(
                authUtil.getCurrentUserIdOptional().orElse(0L),
                authUtil.isAdmin(),
                pageable);

        List<UserSummaryDto> content = usersPage.getContent().stream()
                .map(this::toUserSummary)
                .toList();

        return new PageImpl<>(content, pageable, usersPage.getTotalElements());
    }

    private void requireCanViewUser(User user) {
        if (!canViewUser(user)) {
            throw new AccessDeniedException("Forbidden");
        }
    }

    private boolean canViewUser(User user) {
        if (!Boolean.TRUE.equals(user.getIsPrivate())) {
            return true;
        }

        return authUtil.getCurrentUserOptional()
                .map(currentUser -> currentUser.getRole() == com.example.devforge.entity.enums.Role.ADMIN
                        || currentUser.getId().equals(user.getId()))
                .orElse(false);
    }

    private PageRequest buildPageRequest(int page, int size, String sortBy, String direction) {
        String property = allowedSorts().contains(sortBy) ? sortBy : "id";
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(sortDirection, property));
    }

    private Set<String> allowedSorts() {
        return Set.of("id", "userName", "createdAt", "updatedAt");
    }

    private UserResponseDto toUserResponse(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUserName(user.getUserName());
        dto.setBio(user.getBio());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        dto.setLocation(user.getLocation());
        dto.setSkills(user.getSkills());
        dto.setInterests(user.getInterests());
        dto.setFollowerCount(user.getFollowerCount());
        dto.setFollowingCount(user.getFollowingCount());
        return dto;
    }

    private UserSummaryDto toUserSummary(User user) {
        UserSummaryDto dto = new UserSummaryDto();
        dto.setId(user.getId());
        dto.setUserName(user.getUserName());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        return dto;
    }

}
