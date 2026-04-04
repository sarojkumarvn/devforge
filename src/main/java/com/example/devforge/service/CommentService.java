package com.example.devforge.service;

import java.util.List;

import com.example.devforge.dto.CommentRequestDto;
import com.example.devforge.dto.CommentResponseDto;
import com.example.devforge.dto.ReplyRequestDto;

public interface CommentService {
    void addComment(CommentRequestDto dto) ;

    void replyToComment(Long commentId , ReplyRequestDto dto) ;

    List<CommentResponseDto> getCommentsByProject(Long projectId) ;



}
