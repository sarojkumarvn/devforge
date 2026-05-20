package com.example.devforge.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.devforge.advice.ApiResponse;
import com.example.devforge.dto.CommunityRequestDto;
import com.example.devforge.dto.CommunityResponseDto;
import com.example.devforge.dto.UserResponseDto;
import com.example.devforge.service.CommunityService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping("/users/{userId}/communities")
    public ResponseEntity<CommunityResponseDto> createCommunity( // TESTED
            @PathVariable Long userId,
            @RequestBody CommunityRequestDto dto) {

        return ResponseEntity.ok(
                communityService.createCommunity(userId, dto));
    }

    @GetMapping("/communities")
    public ResponseEntity<List<CommunityResponseDto>> getAllCommunities() { // TESTED

        return ResponseEntity.ok(
                communityService.getAllCommunities());
    }

    @GetMapping("/communities/{communityId}")
    public ResponseEntity<CommunityResponseDto> getCommunityById( // TESTED
            @PathVariable Long communityId) {

        return ResponseEntity.ok(
                communityService.getCommunityById(communityId));
    }

    @PutMapping("/users/{userId}/communities/{communityId}")
    public ResponseEntity<CommunityResponseDto> updateCommunity( // TESTED
            @PathVariable Long userId,
            @PathVariable Long communityId,
            @RequestBody CommunityRequestDto dto) {

        return ResponseEntity.ok(
                communityService.updateCommunity(userId, communityId, dto));
    }

    @DeleteMapping("/users/{userId}/communities/{communityId}") // TESTED
    public ResponseEntity<String> deleteCommunity(
            @PathVariable Long userId,
            @PathVariable Long communityId) {

        communityService.deleteCommunity(userId, communityId);

        return ResponseEntity.ok("Community deleted successfully");
    }

    @PostMapping("/users/{userId}/communities/{communityId}/join")
    public ResponseEntity<ApiResponse<String>> joinCommunity( // TESTED
            @PathVariable Long userId,
            @PathVariable Long communityId) {

        String message = communityService.joinCommunity(userId, communityId);

        return ResponseEntity.ok(
                new ApiResponse<String>(message, message, true));
    }

    @PostMapping("/users/{userId}/communities/{communityId}/leave") // TESTED
    public ResponseEntity<ApiResponse<String>> leaveCommunity(
            @PathVariable Long userId,
            @PathVariable Long communityId) {
        String message = communityService.leaveCommunity(userId, communityId);

        return ResponseEntity.ok(new ApiResponse<String>(message, message, true));
    }

    @GetMapping("/communities/{communityId}/members") // TESTED
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllMembers(
            @PathVariable Long communityId) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Memberd Fetched Successfully",
                        communityService.getAllMembers(communityId),
                        true));
    }

    @GetMapping("/{communityId}/posts") // TESTED
    public ResponseEntity<?> getCommunityPosts(
            @PathVariable Long communityId,
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                communityService.getCommunityPosts(communityId, userId));
    }

}