package com.example.devforge.service;

import java.util.List;

import com.example.devforge.dto.CommentRequestDto;
import com.example.devforge.dto.CommentResponseDto;
import com.example.devforge.dto.ReplyRequestDto;
import com.example.devforge.entity.Comment;

public interface CommentService {
    Comment addComment(CommentRequestDto dto) ;

    Comment replyToComment(Long commentId , ReplyRequestDto dto) ;

    void deleteComment(Long userId , Long commentId) ;

    List<CommentResponseDto> getCommentsByProject(Long projectId) ;



}
