package com.example.devforge.service;

import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.example.devforge.dto.FeedResponseDto;

import com.example.devforge.entity.Project;
import com.example.devforge.entity.enums.Role;

import com.example.devforge.exception.ResourceNotFoundException;
import com.example.devforge.repository.BookMarkRepository;
import com.example.devforge.repository.CommunityMemberRepository;
import com.example.devforge.repository.LikeRepository;
import com.example.devforge.repository.ProjectRepository;
import com.example.devforge.repository.UserRepository;
import com.example.devforge.security.AuthUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AuthUtil authUtil;
    private final CommunityMemberRepository communityMemberRepository;
    private final LikeRepository likeRepository;
    private final BookMarkRepository bookMarkRepository;

    @Override
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Page<FeedResponseDto> getFeed(int page, int size) {
        Long userId = authUtil.getCurrentUserId();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Project> projectPage = projectRepository.findVisibleProjects(userId, authUtil.isAdmin(), pageable);

        return toFeedPage(projectPage, pageable, userId);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Page<FeedResponseDto> getFollowingFeed(int page, int size) {
        Long userId = authUtil.getCurrentUserId();
        userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found with this id : " + userId));
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Project> project = projectRepository.findFollowingFeedProjects(userId, userId, authUtil.isAdmin(), pageable);

        return toFeedPage(project, pageable, userId);

    }



    @Override
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Page<FeedResponseDto> getPopularFeed(int page, int size) {
        Long userId = authUtil.getCurrentUserId();
        PageRequest pageable = PageRequest.of(page , size, Sort.by(Sort.Direction.DESC, "score")) ;
        Page<Project> projects = projectRepository.findPopularFeed(userId, authUtil.isAdmin(), pageable);

        return toFeedPage(projects, pageable, userId);

        
    }

    private Page<FeedResponseDto> toFeedPage(Page<Project> projectPage, Pageable pageable, Long userId) {
        List<Project> visibleProjects = projectPage.getContent();
        List<Long> projectIds = visibleProjects.stream()
                .map(Project::getId)
                .toList();
        if (projectIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        Set<Long> likedProjectIds = Set.copyOf(likeRepository.findLikedProjectIds(userId, projectIds));
        Set<Long> bookmarkedProjectIds = Set.copyOf(bookMarkRepository.findBookmarkedProjectIds(userId, projectIds));

        List<FeedResponseDto> content = visibleProjects.stream()
                .map(project -> mapToDto(project, likedProjectIds, bookmarkedProjectIds))
                .toList();

        return new PageImpl<>(content, pageable, projectPage.getTotalElements());
    }

    private FeedResponseDto mapToDto(Project project, Set<Long> likedProjectIds, Set<Long> bookmarkedProjectIds) {
        FeedResponseDto dto = new FeedResponseDto();
        dto.setProjectId(project.getId());
        dto.setTitle(project.getTitle());
        dto.setDescription(project.getDescription());
        dto.setPhotos(project.getPhotos().toArray(new String[0]));
        dto.setUserId(project.getUser().getId());
        dto.setUserName(project.getUser().getUserName());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setLikeCount(project.getLikeCount());
        dto.setCommentCount(project.getCommentCount());
        dto.setIsLiked(likedProjectIds.contains(project.getId()));
        dto.setIsBookmarked(bookmarkedProjectIds.contains(project.getId()));
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
