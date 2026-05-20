package com.example.devforge.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.devforge.dto.CommentRequestDto;
import com.example.devforge.dto.CommentResponseDto;
import com.example.devforge.dto.CommentUpdateRequestDto;
import com.example.devforge.dto.ReplyRequestDto;
import com.example.devforge.entity.Comment;
import com.example.devforge.entity.Project;
import com.example.devforge.entity.User;
import com.example.devforge.exception.ResourceNotFoundException;
import com.example.devforge.repository.CommentRepository;
import com.example.devforge.repository.ProjectRepository;
import com.example.devforge.repository.UserRepository;
import com.example.devforge.security.AuthUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImple implements CommentService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final CommentRepository commentRepository;
    private final AuthUtil authUtil;

    @Override
    @Transactional
    public CommentResponseDto addComment(CommentRequestDto dto) {
        Long userId = resolveRequestUserId(dto.getUserId());
        validateCommentRequest(dto.getProjectId(), dto.getContent());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this id : " + userId));

        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Comment comment = new Comment();
        comment.setContent(dto.getContent());
        comment.setUser(user);
        comment.setProject(project);
        comment.setCreatedAt(LocalDateTime.now());

        project.incrementCommentCount();
        Comment saved = commentRepository.save(comment);
        projectRepository.save(project);
        return mapToResponse(saved);
    }

    @Transactional
    @Override
    public CommentResponseDto replyToComment(Long commentId, ReplyRequestDto dto) {
        Long userId = resolveRequestUserId(dto.getUserId());
        validateCommentId(commentId);
        validateCommentContent(dto.getContent());

        Comment parent = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Comment not found with id: " + commentId));

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + userId));

        Comment reply = new Comment();
        reply.setContent(dto.getContent());
        reply.setUser(user);
        reply.setProject(parent.getProject());
        reply.setParent(parent);

        parent.getProject().incrementCommentCount();
        Comment saved = commentRepository.save(reply);
        projectRepository.save(parent.getProject());
        return mapToResponse(saved);
    }


    @Override
    public List<CommentResponseDto> getCommentsByProject(Long projectId) {
        List<Comment> comments = commentRepository.findByProjectIdAndParentIsNull(projectId);

        return comments.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        authUtil.requireCurrentUser(userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with this id :" + commentId));

        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not allowed to delete this comment");
        }

        comment.getProject().decrementCommentCount();
        commentRepository.deleteById(commentId);
        projectRepository.save(comment.getProject());
    }

    @Override
    @Transactional
    public CommentResponseDto editComment(Long userId, Long commentId, CommentUpdateRequestDto dto) {
        authUtil.requireCurrentUser(userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with this id : " + commentId));

        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not allowed to edit this comment");
        }

        validateCommentContent(dto.getContent());
        comment.setContent(dto.getContent());

        Comment updated = commentRepository.save(comment);
        return mapToResponse(updated);
    }

    private Long resolveRequestUserId(Long requestUserId) {
        Long currentUserId = authUtil.getCurrentUserId();

        // Frontend may omit userId; the authenticated JWT user is the trusted source.
        if (requestUserId == null) {
            return currentUserId;
        }

        authUtil.requireCurrentUser(requestUserId);
        return requestUserId;
    }

    private void validateCommentRequest(Long projectId, String content) {
        if (projectId == null) {
            throw new RuntimeException("Project id is required");
        }

        validateCommentContent(content);
    }

    private void validateCommentId(Long commentId) {
        if (commentId == null) {
            throw new RuntimeException("Comment id is required");
        }
    }

    private void validateCommentContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Comment content can not be empty");
        }
    }

    private CommentResponseDto mapToResponse(Comment comment) {
        CommentResponseDto dto = new CommentResponseDto();

        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setProjectId(comment.getProject().getId());
        dto.setUserName(comment.getUser().getUserName());
        dto.setReplies(comment.getReplies().stream()
                .map(this::mapToResponse)
                .toList());

        return dto;
    }
}
