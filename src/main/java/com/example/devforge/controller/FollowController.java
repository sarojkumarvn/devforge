package com.example.devforge.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.devforge.advice.ApiResponse;
import com.example.devforge.dto.FollowRequestDto;
import com.example.devforge.dto.FollowResponseDto;
import com.example.devforge.service.FollowService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/follows")
@RequiredArgsConstructor
@Validated
public class FollowController {

    private final FollowService followService;


    @PostMapping   // Tested 
    public ResponseEntity<FollowResponseDto> followUser(@Valid @RequestBody FollowRequestDto dto) {
        FollowResponseDto response = followService.followUser(dto);
        return ResponseEntity.ok(response);
    }

   
   @DeleteMapping
public ResponseEntity<ApiResponse<Object>> unfollow(
    @Valid @RequestBody FollowRequestDto dto
) {
    String message = followService.unfollowUser(dto);
    ApiResponse<Object> response = new ApiResponse<>(message, null, true);
    return ResponseEntity.ok(response);
}
 
    @GetMapping("/{userId}/followers")
    public ResponseEntity<Page<Long>> getFollowers(
            @Positive @PathVariable Long userId,
            @Min(0) @RequestParam(defaultValue = "0") int page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size) {
        Page<Long> followers = followService.getAllFollowers(userId, page, size);
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/{userId}/followings")
    public ResponseEntity<Page<Long>> getFollowings(
            @Positive @PathVariable Long userId,
            @Min(0) @RequestParam(defaultValue = "0") int page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size) {
        Page<Long> followings = followService.getAllFollowings(userId, page, size);
        return ResponseEntity.ok(followings);
    }
}
