package com.example.devforge.service;

import java.util.List;

import com.example.devforge.dto.UserRequestDto;
import com.example.devforge.dto.UserResponseDto;
import com.example.devforge.dto.UserSummaryDto;
import com.example.devforge.dto.UserUpdateDto;

public interface UserService {
    UserResponseDto createUser(UserRequestDto dto);

    UserResponseDto getUserById(Long userId);

    List<UserSummaryDto> getAllUsers(int page, int size);

    UserResponseDto updateUser(Long userId, UserUpdateDto dto);

    void deleteUser(Long userId);

}
