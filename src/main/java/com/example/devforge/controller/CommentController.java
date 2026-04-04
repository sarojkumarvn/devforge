package com.example.devforge.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.devforge.dto.CommentRequestDto;
import com.example.devforge.dto.CommentResponseDto;
import com.example.devforge.dto.ReplyRequestDto;
import com.example.devforge.service.CommentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor

public class CommentController {
    private final CommentService commentService;

    // Add Comment
    @PostMapping
    public ResponseEntity<String> addComment(
            @RequestBody CommentRequestDto dto) {
        commentService.addComment(dto);

        return ResponseEntity.ok("Comment added successfully");
    }

    // Reply to comment

    @PostMapping("/{commentId}/reply")
    public ResponseEntity<String> replyToComment(
            @PathVariable Long commentId,
            @RequestBody ReplyRequestDto request

    ) {
        commentService.replyToComment(commentId, request);
        return ResponseEntity.ok("Replied to the comment!");

    }

    // Get comments by project

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List
    <CommentResponseDto>> getCommentsOfProject(
        @PathVariable Long projectId 
        
    ) {
        return ResponseEntity.ok(commentService.getCommentsByProject(projectId));

    }
}
