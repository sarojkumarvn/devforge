package com.example.devforge.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.devforge.dto.ProjectResponseDto;
import com.example.devforge.service.LikeService;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping()
@RequiredArgsConstructor
@Validated
public class LikeController {

    private final LikeService likeService;

    // Like a project
    @PostMapping("/projects/{projectId}/likes")   // Testing Done 
    public ResponseEntity<Void> likeProject(
            @Positive @PathVariable Long projectId,
            @RequestParam(required = false) Long userId) {
        likeService.likeProject(projectId);
        return ResponseEntity.noContent().build(); 
    }

    // Unlike a project
    @DeleteMapping("/projects/{projectId}/likes/{userId}")  // Testing Done
    public ResponseEntity<Void> unlikeProject(
            @Positive @PathVariable Long projectId,
            @Positive @PathVariable Long userId) {
        likeService.unlikeProject(projectId);
        return ResponseEntity.noContent().build();
    }

    // Get all liked projects
    @GetMapping("/users/{userId}/likes")   // Testing Done 
    public ResponseEntity<List<ProjectResponseDto>> getUserLikes(
            @Positive @PathVariable Long userId,
            @RequestParam(required = false) String filter) {

        return ResponseEntity.ok(
                likeService.getLikedProjectsLast90Days());

    }
}
