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
import com.example.devforge.service.BookMarkService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookMarkController {
    private final BookMarkService bookMarkService ;

   
    @PostMapping
    public ResponseEntity<String> bookmarkProject(
            @RequestParam Long userId,
            @RequestParam Long projectId
    ) {
        bookMarkService.bookmarkProject(userId, projectId);
        return ResponseEntity.ok("Project bookmarked");
    }

   
    @DeleteMapping
    public ResponseEntity<String> removeBookmark(
            @RequestParam Long userId,
            @RequestParam Long projectId
    ) {
        bookMarkService.removeBookmark(userId, projectId);
        return ResponseEntity.ok("Bookmark removed");
    }

    @GetMapping("/{userId}/recent")
    public ResponseEntity<List<ProjectResponseDto>> getRecentBookmarks(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                bookMarkService.getBookmarkedProjectsLast90Days(userId)
        );
    }


}
