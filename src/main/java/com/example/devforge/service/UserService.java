package com.example.devforge.service;

import org.springframework.data.domain.Page;
import com.example.devforge.dto.UserCreateRequestDto;
import com.example.devforge.dto.UserResponseDto;
import com.example.devforge.dto.UserSummaryDto;
import com.example.devforge.dto.UserUpdateDto;

public interface UserService {
    UserResponseDto createUser(UserCreateRequestDto dto);

    UserResponseDto getUserById(Long userId);

    Page<UserSummaryDto> getAllUsers(int page, int size, String sortBy, String direction);

    UserResponseDto updateUser(Long userId, UserUpdateDto dto);

    void deleteUser(Long userId);

}
