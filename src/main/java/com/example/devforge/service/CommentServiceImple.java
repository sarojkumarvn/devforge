package com.example.devforge.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.devforge.dto.CommentRequestDto;
import com.example.devforge.dto.CommentResponseDto;
import com.example.devforge.dto.CommentUpdateRequestDto;
import com.example.devforge.dto.ReplyRequestDto;
import com.example.devforge.entity.Comment;
import com.example.devforge.entity.Project;
import com.example.devforge.entity.User;
import com.example.devforge.entity.enums.Role;
import com.example.devforge.exception.BadRequestException;
import com.example.devforge.exception.ForbiddenException;
import com.example.devforge.exception.ResourceNotFoundException;
import com.example.devforge.repository.CommentRepository;
import com.example.devforge.repository.CommunityMemberRepository;
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
    private final CommunityMemberRepository communityMemberRepository;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @CacheEvict(cacheNames = {"projectComments", "projects", "projectPages", "feedPages", "communityPosts"}, allEntries = true)
    public CommentResponseDto addComment(CommentRequestDto dto) {
        Long userId = authUtil.getCurrentUserId();
        validateCommentRequest(dto.getProjectId(), dto.getContent());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this id : " + userId));

        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        requireCanViewProject(project);

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
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @CacheEvict(cacheNames = {"projectComments", "projects", "projectPages", "feedPages", "communityPosts"}, allEntries = true)
    public CommentResponseDto replyToComment(Long commentId, ReplyRequestDto dto) {
        Long userId = authUtil.getCurrentUserId();
        validateCommentId(commentId);
        validateCommentContent(dto.getContent());

        Comment parent = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Comment not found with id: " + commentId));
        requireCanViewProject(parent.getProject());

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
    public Page<CommentResponseDto> getCommentsByProject(Long projectId, int page, int size, String direction) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        requireCanViewProject(project);

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

        return commentRepository.findByProjectIdAndParentIsNull(
                        projectId,
                        PageRequest.of(page, size, Sort.by(sortDirection, "createdAt")))
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @CacheEvict(cacheNames = {"projectComments", "projects", "projectPages", "feedPages", "communityPosts"}, allEntries = true)
    public void deleteComment(Long userId, Long commentId) {
        authUtil.requireCurrentUser(userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with this id :" + commentId));

        if (!comment.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not allowed to delete this comment");
        }

        comment.getProject().decrementCommentCount();
        commentRepository.deleteById(commentId);
        projectRepository.save(comment.getProject());
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @CacheEvict(cacheNames = "projectComments", allEntries = true)
    public CommentResponseDto editComment(Long userId, Long commentId, CommentUpdateRequestDto dto) {
        authUtil.requireCurrentUser(userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with this id : " + commentId));

        if (!comment.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not allowed to edit this comment");
        }

        validateCommentContent(dto.getContent());
        comment.setContent(dto.getContent());

        Comment updated = commentRepository.save(comment);
        return mapToResponse(updated);
    }

    private void validateCommentRequest(Long projectId, String content) {
        if (projectId == null) {
            throw new BadRequestException("Project id is required");
        }

        validateCommentContent(content);
    }

    private void validateCommentId(Long commentId) {
        if (commentId == null) {
            throw new BadRequestException("Comment id is required");
        }
    }

    private void validateCommentContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BadRequestException("Comment content can not be empty");
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

    private void requireCanViewProject(Project project) {
        if (!canViewProject(project)) {
            throw new AccessDeniedException("Forbidden");
        }
    }

    private boolean canViewProject(Project project) {
        if (Boolean.TRUE.equals(project.getIsPublic())) {
            return true;
        }

        return authUtil.getCurrentUserOptional()
                .map(user -> user.getRole() == Role.ADMIN
                        || user.getId().equals(project.getUser().getId())
                        || (project.getCommunity() != null
                                && communityMemberRepository.existsByUserIdAndCommunityId(
                                        user.getId(), project.getCommunity().getId())))
                .orElse(false);
    }
}
