package com.example.devforge.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.devforge.advice.ApiResponse;
import com.example.devforge.dto.CommunityCreateRequestDto;
import com.example.devforge.dto.CommunityResponseDto;
import com.example.devforge.dto.CommunityUpdateRequestDto;
import com.example.devforge.dto.UserResponseDto;
import com.example.devforge.service.CommunityService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Validated
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping("/users/{userId}/communities")
    public ResponseEntity<CommunityResponseDto> createCommunity( // TESTED
            @Positive @PathVariable Long userId,
            @Valid @RequestBody CommunityCreateRequestDto dto) {

        return ResponseEntity.ok(
                communityService.createCommunity(userId, dto));
    }

    @GetMapping("/communities")
    public ResponseEntity<Page<CommunityResponseDto>> getAllCommunities(
            @Min(0) @RequestParam(defaultValue = "0") int page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) { // TESTED

        return ResponseEntity.ok(
                communityService.getAllCommunities(page, size, sortBy, direction));
    }

    @GetMapping("/communities/{communityId}")
    public ResponseEntity<CommunityResponseDto> getCommunityById( // TESTED
            @Positive @PathVariable Long communityId) {

        return ResponseEntity.ok(
                communityService.getCommunityById(communityId));
    }

    @PutMapping("/users/{userId}/communities/{communityId}")
    public ResponseEntity<CommunityResponseDto> updateCommunity( // TESTED
            @Positive @PathVariable Long userId,
            @Positive @PathVariable Long communityId,
            @Valid @RequestBody CommunityUpdateRequestDto dto) {

        return ResponseEntity.ok(
                communityService.updateCommunity(userId, communityId, dto));
    }

    @DeleteMapping("/users/{userId}/communities/{communityId}") // TESTED
    public ResponseEntity<String> deleteCommunity(
            @Positive @PathVariable Long userId,
            @Positive @PathVariable Long communityId) {

        communityService.deleteCommunity(userId, communityId);

        return ResponseEntity.ok("Community deleted successfully");
    }

    @PostMapping("/users/{userId}/communities/{communityId}/join")
    public ResponseEntity<ApiResponse<String>> joinCommunity( // TESTED
            @Positive @PathVariable Long userId,
            @Positive @PathVariable Long communityId) {

        String message = communityService.joinCommunity(userId, communityId);

        return ResponseEntity.ok(
                new ApiResponse<String>(message, message, true));
    }

    @PostMapping("/users/{userId}/communities/{communityId}/leave") // TESTED
    public ResponseEntity<ApiResponse<String>> leaveCommunity(
            @Positive @PathVariable Long userId,
            @Positive @PathVariable Long communityId) {
        String message = communityService.leaveCommunity(userId, communityId);

        return ResponseEntity.ok(new ApiResponse<String>(message, message, true));
    }

    @GetMapping("/communities/{communityId}/members") // TESTED
    public ResponseEntity<ApiResponse<Page<UserResponseDto>>> getAllMembers(
            @Positive @PathVariable Long communityId,
            @Min(0) @RequestParam(defaultValue = "0") int page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Memberd Fetched Successfully",
                        communityService.getAllMembers(communityId, page, size),
                        true));
    }

    @GetMapping("/{communityId}/posts") // TESTED
    public ResponseEntity<?> getCommunityPosts(
            @Positive @PathVariable Long communityId,
            @Positive @RequestParam(required = false) Long userId,
            @Min(0) @RequestParam(defaultValue = "0") int page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        return ResponseEntity.ok(
                communityService.getCommunityPosts(communityId, page, size, sortBy, direction));
    }

}
