package com.example.devforge.service;

import java.util.List;

import com.example.devforge.dto.CommentRequestDto;
import com.example.devforge.dto.CommentResponseDto;
import com.example.devforge.dto.CommentUpdateRequestDto;
import com.example.devforge.dto.ReplyRequestDto;

public interface CommentService {
    CommentResponseDto addComment(CommentRequestDto dto) ;

    CommentResponseDto replyToComment(Long commentId , ReplyRequestDto dto) ;

    void deleteComment(Long userId , Long commentId) ;

    List<CommentResponseDto> getCommentsByProject(Long projectId) ;

    CommentResponseDto editComment(Long userId , Long commentId , CommentUpdateRequestDto dto);


  



}
