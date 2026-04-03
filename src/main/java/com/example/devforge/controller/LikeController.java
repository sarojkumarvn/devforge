package com.example.devforge.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.devforge.dto.ProjectResponseDto;
import com.example.devforge.service.LikeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    @PostMapping
    public ResponseEntity<String> likeProject(
            @RequestParam Long userId,
            @RequestParam Long projectId

    ) {
        likeService.likeProject(userId, projectId);
        return ResponseEntity.ok("Project liked");
    }

    @DeleteMapping
    public ResponseEntity<String> unlikeProject(
            @RequestParam Long userId,
            @RequestParam Long projectId) {
        likeService.unlikeProject(userId, projectId);
        return ResponseEntity.ok("Project unliked");
    }

    @GetMapping("/{userId}/recent")
    public ResponseEntity<List<ProjectResponseDto>> getRecentLikes(
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                likeService.getLikedProjectsLast90Days(userId));
    }

}
