package com.example.devforge.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.devforge.dto.ProjectResponseDto;
import com.example.devforge.entity.Like;
import com.example.devforge.entity.Project;
import com.example.devforge.entity.User;
import com.example.devforge.entity.enums.Role;
import com.example.devforge.exception.ConflictException;
import com.example.devforge.exception.ResourceNotFoundException;
import com.example.devforge.repository.CommunityMemberRepository;
import com.example.devforge.repository.LikeRepository;
import com.example.devforge.repository.ProjectRepository;
import com.example.devforge.repository.UserRepository;
import com.example.devforge.security.AuthUtil;
import com.example.devforge.strategy.FeedScoreStrategy;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeServiceImple implements LikeService {

    private final LikeRepository likeRepository ;
    private final UserRepository userRepository ;
    private final ProjectRepository projectRepository ;
    private final FeedScoreStrategy feedScoreStrategy ;
    private final AuthUtil authUtil;
    private final CommunityMemberRepository communityMemberRepository;
    

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @CacheEvict(cacheNames = {"projects", "projectPages", "feedPages", "communityPosts", "likedProjects"}, allEntries = true)
    public void likeProject(Long projectId) {
           Long userId = authUtil.getCurrentUserId();

           if (likeRepository.findByUserIdAndProjectId(userId, projectId).isPresent()) {
            throw new ConflictException("Already liked this project");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        requireCanViewProject(project);

        Like like = new Like();
        like.setUser(user);
        like.setProject(project);
        like.setCreatedAt(LocalDateTime.now());

        project.incrementLikeCount();
        double newScore = feedScoreStrategy.calculateScore(project);
        project.setScore(newScore);

        likeRepository.save(like);
        projectRepository.save(project);
        
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @CacheEvict(cacheNames = {"projects", "projectPages", "feedPages", "communityPosts", "likedProjects"}, allEntries = true)
    public void unlikeProject(Long projectId) {
        Long userId = authUtil.getCurrentUserId();

        Like like = likeRepository.findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Like not found"));
        requireCanViewProject(like.getProject());

        like.getProject().decrementLikeCount();
        likeRepository.delete(like);
        projectRepository.save(like.getProject());
    }

    @Override
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Cacheable(cacheNames = "likedProjects", key = "@authUtil.getCurrentCacheUserId()")
    public List<ProjectResponseDto> getLikedProjectsLast90Days() {
        Long userId = authUtil.getCurrentUserId();

        LocalDateTime last90days = LocalDateTime.now().minusDays(90);


        return likeRepository
                .findByUserIdAndCreatedAtAfter(userId, last90days)
                .stream()
                .filter(like -> canViewProject(like.getProject()))
                .map(like -> mapToResponse(like.getProject()))
                .toList();
                

    }

    private void requireCanViewProject(Project project) {
        if (!canViewProject(project)) {
            throw new AccessDeniedException("Forbidden");
        }
    }

    private ProjectResponseDto mapToResponse(Project project) {
        ProjectResponseDto dto = new ProjectResponseDto();
        dto.setId(project.getId());
        dto.setTitle(project.getTitle());
        dto.setDescription(project.getDescription());
        dto.setGithubLink(project.getGithubLink());
        dto.setLiveDemoLink(project.getLiveDemoLink());
        dto.setTechStacks(Set.copyOf(project.getTechStacks()));
        dto.setStatus(project.getStatus());
        dto.setPhotos(project.getPhotos().toArray(new String[0]));
        dto.setUserId(project.getUser().getId());
        dto.setUserName(project.getUser().getUserName());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setIsPublic(project.getIsPublic());
        dto.setLikeCount(project.getLikeCount());
        dto.setCommentCount(project.getCommentCount());
        dto.setBookmarkCount(project.getBookmarkCount());
        if (project.getCommunity() != null) {
            dto.setCommunityId(project.getCommunity().getId());
        }
        return dto;
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
