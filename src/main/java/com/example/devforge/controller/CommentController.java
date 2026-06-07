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

import com.example.devforge.advice.ApiResponse;
import com.example.devforge.dto.CommentRequestDto;
import com.example.devforge.dto.CommentResponseDto;
import com.example.devforge.dto.CommentUpdateRequestDto;
import com.example.devforge.dto.ReplyRequestDto;
import com.example.devforge.service.CommentService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

// TESTED 
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Validated

public class CommentController {
    private final CommentService commentService;

    // Add Comment
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponseDto>> addComment(@Valid @RequestBody CommentRequestDto dto) {
        CommentResponseDto response = commentService.addComment(dto);
        return ResponseEntity.ok(
                new ApiResponse<>("Comment added successfully", response, true));
    }

    // Reply to comment

    @PostMapping("/{commentId}/reply")
    public ResponseEntity<ApiResponse<CommentResponseDto>> replyToComment(
            @Positive @PathVariable Long commentId,
            @Valid @RequestBody ReplyRequestDto request) {
        CommentResponseDto response = commentService.replyToComment(commentId, request);

        return ResponseEntity.ok(
                new ApiResponse<>("Reply added successfully", response, true));
    }

    // Get comments by project

    @GetMapping("/projects/{projectId}")
    public ResponseEntity<Page<CommentResponseDto>> getCommentsOfProject(
            @Positive @PathVariable Long projectId,
            @Min(0) @RequestParam(defaultValue = "0") int page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String direction

    ) {
        return ResponseEntity.ok(commentService.getCommentsByProject(projectId, page, size, direction));

    }




    // Delete comment
    @DeleteMapping("/{userId}/{commentId}")  // Tested 
    public ResponseEntity<Void> deleteComment(
            @Positive @PathVariable Long userId,
            @Positive @PathVariable Long commentId) {

        commentService.deleteComment(userId, commentId);

        return ResponseEntity.noContent().build();

    }

    @PutMapping("/{userId}/{commentId}")   // Tested
    public ResponseEntity<CommentResponseDto> updateComment(
            @Positive @PathVariable Long userId,
            @Positive @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequestDto dto

    )

    {
        CommentResponseDto response = commentService.editComment(userId, commentId, dto);

        return ResponseEntity.ok(response);
    }
}
