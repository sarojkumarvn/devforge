package com.example.devforge.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.devforge.dto.UserCreateRequestDto;
import com.example.devforge.dto.UserResponseDto;
import com.example.devforge.dto.UserSummaryDto;
import com.example.devforge.dto.UserUpdateDto;

import com.example.devforge.service.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserService userService;

    // create user
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
            @Valid @RequestBody UserCreateRequestDto dto) {
        UserResponseDto response = userService.createUser(dto);
        return ResponseEntity.ok(response);

    }

    // Get user by id
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUserById(
            @Positive @PathVariable Long userId) {
        UserResponseDto response = userService.getUserById(userId);
        return ResponseEntity.ok(response);

    }

    // Get all the users
    @GetMapping
    public ResponseEntity<Page<UserSummaryDto>> getAllUsers(
            @Min(0) @RequestParam(defaultValue = "0") int page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(userService.getAllUsers(page, size, sortBy, direction));
    }

    // update the user details
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateDto request) {
        UserResponseDto response = userService.updateUser(userId, request);

        return ResponseEntity.ok(response);

    }

    // delete the user
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @Positive @PathVariable Long userId) {

        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

}
