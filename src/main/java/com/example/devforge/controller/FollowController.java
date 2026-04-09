package com.example.devforge.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.devforge.advice.ApiResponse;
import com.example.devforge.dto.FollowRequestDto;
import com.example.devforge.dto.FollowResponseDto;
import com.example.devforge.service.FollowService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;


    @PostMapping   // Tested 
    public ResponseEntity<FollowResponseDto> followUser(@RequestBody FollowRequestDto dto) {
        FollowResponseDto response = followService.followUser(dto);
        return ResponseEntity.ok(response);
    }

   
   @DeleteMapping
public ResponseEntity<ApiResponse> unfollow(
    @RequestBody FollowRequestDto dto
) {
    ApiResponse response = new ApiResponse("Unfollowed successfully", null, true);
    return ResponseEntity.ok(response);
}
 
    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<Long>> getFollowers(@PathVariable Long userId) {
        List<Long> followers = followService.getAllFollowers(userId);
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/{userId}/followings")
    public ResponseEntity<List<Long>> getFollowings(@PathVariable Long userId) {
        List<Long> followings = followService.getAllFollowings(userId);
        return ResponseEntity.ok(followings);
    }
}