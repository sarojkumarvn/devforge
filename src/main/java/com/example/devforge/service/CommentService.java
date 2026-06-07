package com.example.devforge.service;

import org.springframework.data.domain.Page;
import com.example.devforge.dto.CommentRequestDto;
import com.example.devforge.dto.CommentResponseDto;
import com.example.devforge.dto.CommentUpdateRequestDto;
import com.example.devforge.dto.ReplyRequestDto;

public interface CommentService {
    CommentResponseDto addComment(CommentRequestDto dto) ;

    CommentResponseDto replyToComment(Long commentId , ReplyRequestDto dto) ;

    void deleteComment(Long userId , Long commentId) ;

    Page<CommentResponseDto> getCommentsByProject(Long projectId, int page, int size, String direction) ;

    CommentResponseDto editComment(Long userId , Long commentId , CommentUpdateRequestDto dto);


  



}
