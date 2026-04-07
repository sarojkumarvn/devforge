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

import com.example.devforge.advice.ApiResponse;
import com.example.devforge.dto.ProjectResponseDto;
import com.example.devforge.service.BookMarkService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookMarkController {
    private final BookMarkService bookMarkService ;

   
 @PostMapping("/bookmark")
public ResponseEntity<ApiResponse<Object>> bookmarkProject(
        @RequestParam Long userId,
        @RequestParam Long projectId
) {
    bookMarkService.bookmarkProject(userId, projectId);

    return ResponseEntity.ok(
            new ApiResponse<>("Project bookmarked successfully", null, true)
    );
}

   
    @DeleteMapping
    public ResponseEntity<ApiResponse<Object>> removeBookmark(
            @RequestParam Long userId,
            @RequestParam Long projectId
    ) {
        bookMarkService.removeBookmark(userId, projectId);
         return ResponseEntity.ok(
            new ApiResponse<>("BookMark removed successfully", null, true)
    );
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
