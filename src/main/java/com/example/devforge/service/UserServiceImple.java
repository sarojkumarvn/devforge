package com.example.devforge.service;

import java.time.LocalDate;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.devforge.dto.UserRequestDto;
import com.example.devforge.dto.UserResponseDto;
import com.example.devforge.dto.UserSummaryDto;
import com.example.devforge.dto.UserUpdateDto;
import com.example.devforge.entity.User;
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
    private final ModelMapper modelMapper;
    private final FollowRepository followRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthUtil authUtil;

    @Override
    public UserResponseDto createUser(UserRequestDto dto) {

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        if (userRepository.findByUserName(dto.getUserName()).isPresent()) {
            throw new RuntimeException("Username already taken");
        }

        LocalDate today = LocalDate.now();
        if (dto.getDateOfBirth() != null
                && (dto.getDateOfBirth().isAfter(today) || dto.getDateOfBirth().isEqual(today))) {
            throw new RuntimeException("Your Date of birth is invalid ");
        }

        User user = modelMapper.map(dto, User.class);

        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        User savedUser = userRepository.save(user);

        return modelMapper.map(savedUser, UserResponseDto.class);
    }

    @Override
    public UserResponseDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this id : {}" + userId));

        UserResponseDto response = modelMapper.map(user, UserResponseDto.class);
        Long followerCount = followRepository.countByFollowingId(userId);
        Long followingCount = followRepository.countByFollowerId(userId);

        response.setFollowerCount(followerCount);
        response.setFollowingCount(followingCount);

        return response;
    }

    @Override
    public UserResponseDto updateUser(Long userId, UserUpdateDto dto) {
        authUtil.requireCurrentUserOrAdmin(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (dto.getUserName() != null && !dto.getUserName().equals(user.getUserName())
                && userRepository.findByUserName(dto.getUserName()).isPresent()) {
            throw new RuntimeException("Username already taken");
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

        return modelMapper.map(updatedUser, UserResponseDto.class);
    }

    @Override
    public void deleteUser(Long userId) {
        authUtil.requireCurrentUserOrAdmin(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this id : {}" + userId));
        userRepository.delete(user);

    }

    @Override
    public List<UserSummaryDto> getAllUsers(int page, int size) {

        Page<User> usersPage = userRepository.findAll(
                PageRequest.of(page, size));

        return usersPage
                .getContent()
                .stream()
                .map(user -> modelMapper.map(user, UserSummaryDto.class))
                .toList();
    }

}
