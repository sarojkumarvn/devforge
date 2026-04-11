package com.example.devforge.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.devforge.advice.ApiResponse;
import com.example.devforge.dto.CommentRequestDto;
import com.example.devforge.dto.CommentResponseDto;
import com.example.devforge.dto.CommentUpdateRequestDto;
import com.example.devforge.dto.ReplyRequestDto;
import com.example.devforge.service.CommentService;

import lombok.RequiredArgsConstructor;

// TESTED 
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor

public class CommentController {
    private final CommentService commentService;

    // Add Comment
    @PostMapping
    public ResponseEntity<ApiResponse<Object>> addComment(@RequestBody CommentRequestDto dto) {
        commentService.addComment(dto);
        return ResponseEntity.ok(
                new ApiResponse<>("Comment added successfully"));
    }

    // Reply to comment

    @PostMapping("/{commentId}/reply")
    public ResponseEntity<ApiResponse<Object>> replyToComment(
            @PathVariable Long commentId,
            @RequestBody ReplyRequestDto request) {
        commentService.replyToComment(commentId, request);

        return ResponseEntity.ok(
                new ApiResponse<>("Reply added successfully", null, true));
    }

    // Get comments by project

    @GetMapping("/projects/{projectId}")
    public ResponseEntity<List<CommentResponseDto>> getCommentsOfProject(
            @PathVariable Long projectId

    ) {
        return ResponseEntity.ok(commentService.getCommentsByProject(projectId));

    }

    // TODO ----> Edit comment

    @DeleteMapping("/{userId}/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long userId,
            @PathVariable Long commentId) {

        commentService.deleteComment(userId, commentId);

        return ResponseEntity.noContent().build();

    }

    @PutMapping("/{userId}/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long userId,
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequestDto dto

    )

    {
        CommentResponseDto response = commentService.editComment(userId, commentId, dto);

        return ResponseEntity.ok(response);
    }
}
