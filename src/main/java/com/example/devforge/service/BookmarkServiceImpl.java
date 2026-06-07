package com.example.devforge.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.example.devforge.dto.ProjectResponseDto;
import com.example.devforge.entity.BookMark;
import com.example.devforge.entity.Project;
import com.example.devforge.entity.User;
import com.example.devforge.entity.enums.Role;
import com.example.devforge.exception.ConflictException;
import com.example.devforge.exception.ResourceNotFoundException;
import com.example.devforge.repository.BookMarkRepository;
import com.example.devforge.repository.CommunityMemberRepository;
import com.example.devforge.repository.ProjectRepository;
import com.example.devforge.repository.UserRepository;
import com.example.devforge.security.AuthUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookMarkService {

    private final BookMarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final AuthUtil authUtil;
    private final CommunityMemberRepository communityMemberRepository;

    @Override
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @CacheEvict(cacheNames = {"projects", "projectPages", "feedPages", "communityPosts", "bookmarkedProjects"}, allEntries = true)
    public void bookmarkProject(Long projectId) {
        Long userId = authUtil.getCurrentUserId();

        if (bookmarkRepository.findByUserIdAndProjectId(userId, projectId).isPresent()) {
            throw new ConflictException("Already bookmarked");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        requireCanViewProject(project);

        BookMark bookmark = new BookMark();

        bookmark.setUser(user);
        bookmark.setProject(project);
        bookmark.setCreatedAt(LocalDateTime.now());

        project.incrementBookmarkCount();

        bookmarkRepository.save(bookmark);
        projectRepository.save(project);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @CacheEvict(cacheNames = {"projects", "projectPages", "feedPages", "communityPosts", "bookmarkedProjects"}, allEntries = true)
    public void removeBookmark(Long projectId) {
        Long userId = authUtil.getCurrentUserId();

        BookMark bookmark = bookmarkRepository
                .findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark not found"));
        requireCanViewProject(bookmark.getProject());

        bookmark.getProject().decrementBookmarkCount();

        bookmarkRepository.delete(bookmark);

        projectRepository.save(bookmark.getProject());
    }

    @Override
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Cacheable(cacheNames = "bookmarkedProjects", key = "@authUtil.getCurrentCacheUserId()")
    public List<ProjectResponseDto> getBookmarkedProjectsLast90Days() {
        Long userId = authUtil.getCurrentUserId();

        LocalDateTime last90Days = LocalDateTime.now().minusDays(90);

        return bookmarkRepository
                .findByUserIdAndCreatedAtAfter(userId, last90Days)
                .stream()
                .filter(bookmark -> canViewProject(bookmark.getProject()))
                .map(bookmark -> mapToResponse(bookmark.getProject()))
                .toList();
    }

    private ProjectResponseDto mapToResponse(Project project) {

        ProjectResponseDto dto = new ProjectResponseDto();

        dto.setId(project.getId());
        dto.setTitle(project.getTitle());
        dto.setDescription(project.getDescription());

        dto.setGithubLink(project.getGithubLink());
        dto.setLiveDemoLink(project.getLiveDemoLink());

        dto.setIsPublic(project.getIsPublic());

        dto.setPhotos(List.copyOf(project.getPhotos()).toArray(new String[0]));
        dto.setTechStacks(Set.copyOf(project.getTechStacks()));

        dto.setLikeCount(project.getLikeCount());
        dto.setCommentCount(project.getCommentCount());
        dto.setBookmarkCount(project.getBookmarkCount());

        dto.setCreatedAt(project.getCreatedAt());

        dto.setUserId(project.getUser().getId());
        dto.setUserName(project.getUser().getUserName());

        if (project.getCommunity() != null) {
            dto.setCommunityId(project.getCommunity().getId());
        }

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
