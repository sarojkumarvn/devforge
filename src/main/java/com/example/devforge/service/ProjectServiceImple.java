package com.example.devforge.service;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.devforge.dto.ProjectCreateRequestDto;
import com.example.devforge.dto.ProjectResponseDto;
import com.example.devforge.dto.ProjectUpdateRequestDto;
import com.example.devforge.entity.Community;
import com.example.devforge.entity.Project;
import com.example.devforge.entity.User;
import com.example.devforge.exception.BadRequestException;
import com.example.devforge.exception.ForbiddenException;
import com.example.devforge.exception.ResourceNotFoundException;
import com.example.devforge.repository.CommunityMemberRepository;
import com.example.devforge.repository.CommunityRepository;
import com.example.devforge.repository.ProjectRepository;
import com.example.devforge.repository.UserRepository;
import com.example.devforge.security.AuthUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
// Keeps the Hibernate session open while ModelMapper reads lazy project fields into DTOs.
@Transactional(readOnly = true)
public class ProjectServiceImple implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final AuthUtil authUtil;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @CacheEvict(cacheNames = {"projectPages", "feedPages", "communityPosts", "likedProjects", "bookmarkedProjects"}, allEntries = true)
    public ProjectResponseDto createProject(Long userId, ProjectCreateRequestDto dto) {

    authUtil.requireCurrentUser(userId);

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    // MANUAL ENTITY CREATION
    Project project = new Project();

    project.setTitle(dto.getTitle());
    project.setDescription(dto.getDescription());

    project.setGithubLink(dto.getGithubLink());
    project.setLiveDemoLink(dto.getLiveDemoLink());

    project.setTechStacks(dto.getTechStacks());
    project.setPhotos(dto.getPhotos() == null ? new ArrayList<>() : Arrays.asList(dto.getPhotos()));

    project.setUser(user);

    // PUBLIC PROJECT
    if (Boolean.TRUE.equals(dto.getIsPublic())) {

        project.setIsPublic(true);
        project.setCommunity(null);

    }

    // COMMUNITY PROJECT
    else {

        if (dto.getCommunityId() == null) {
            throw new BadRequestException("Community id required");
        }

        boolean isMember = communityMemberRepository
                .existsByUserIdAndCommunityId(userId, dto.getCommunityId());

        if (!isMember) {
            throw new ForbiddenException("You are not a member of this community");
        }

        Community community = communityRepository.findById(dto.getCommunityId())
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));

        project.setIsPublic(false);
        project.setCommunity(community);
    }

    Project saved = projectRepository.save(project);

    return toProjectResponse(saved);
}
    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @CachePut(cacheNames = "projects", key = "#projectId + ':viewer:' + @authUtil.getCurrentCacheUserId()")
    @CacheEvict(cacheNames = {"projectPages", "feedPages", "communityPosts", "likedProjects", "bookmarkedProjects"}, allEntries = true)
    public ProjectResponseDto updateProject(Long userId, Long projectId, ProjectUpdateRequestDto dto) {
        authUtil.requireCurrentUserOrAdmin(userId);
        log.debug("Updating project. userId={}, projectId={}", userId, projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with this id :" + projectId));

        if (!project.getUser().getId().equals(userId) && !authUtil.isAdmin()) {
            throw new ForbiddenException("You are not allowed to update this project");

        }

        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setGithubLink(dto.getGithubLink());
        project.setLiveDemoLink(dto.getLiveDemoLink());
        project.setTechStacks(dto.getTechStacks());
        project.setPhotos(dto.getPhotos() == null ? new ArrayList<>() : Arrays.asList(dto.getPhotos()));

        Project updated = projectRepository.save(project);

        return toProjectResponse(updated);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @CacheEvict(cacheNames = {"projects", "projectPages", "feedPages", "communityPosts", "projectComments", "likedProjects", "bookmarkedProjects"}, allEntries = true)
    public void deleteProject(Long userId, Long projectId) {
        authUtil.requireCurrentUserOrAdmin(userId);
        log.info("Deleting the project with the project ID :  {}" + projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with this id : {}" + projectId));

        if (!project.getUser().getId().equals(userId) && !authUtil.isAdmin()) {
            throw new ForbiddenException("You are not allowed to delete this project");

        }
        projectRepository.delete(project);

    }

    @Override
    @Cacheable(cacheNames = "projects", key = "#projectId + ':viewer:' + @authUtil.getCurrentCacheUserId()")
    public ProjectResponseDto getProjectById(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with this id : {}" + projectId));
        requireCanViewProject(project);
        return toProjectResponse(project);

    }

    @Override
    public Page<ProjectResponseDto> getAllProjects(int page, int size, String sortBy, String direction) {
        Pageable pageable = buildPageRequest(page, size, sortBy, direction);
        return projectRepository.findVisibleProjects(currentViewerId(), authUtil.isAdmin(), pageable)
                .map(this::toProjectResponse);
    }

    @Override
    public Page<ProjectResponseDto> getProjectsByUser(Long userId, int page, int size, String sortBy, String direction) {

        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Pageable pageable = buildPageRequest(page, size, sortBy, direction);
        return projectRepository.findVisibleByUserId(userId, currentViewerId(), authUtil.isAdmin(), pageable)
                .map(this::toProjectResponse);
    }

    @Override
    public Page<ProjectResponseDto> searchProjects(String keyword, int page, int size, String sortBy, String direction) {
        if (keyword == null || keyword.trim().isEmpty()) { // checking of the keyword is empty or what
            throw new BadRequestException("Search keyword can not be empty");

        }

        Pageable pageable = buildPageRequest(page, size, sortBy, direction);
        return projectRepository.searchVisibleByTitle(keyword, currentViewerId(), authUtil.isAdmin(), pageable)
                .map(this::toProjectResponse);

    }

    private PageRequest buildPageRequest(int page, int size, String sortBy, String direction) {
        String property = allowedSorts().contains(sortBy) ? sortBy : "createdAt";
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(sortDirection, property));
    }

    private Set<String> allowedSorts() {
        return Set.of("createdAt", "updatedAt", "title", "score", "likeCount", "commentCount", "bookmarkCount");
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
                .map(user -> user.getRole() == com.example.devforge.entity.enums.Role.ADMIN
                        || user.getId().equals(project.getUser().getId())
                        || (project.getCommunity() != null
                                && communityMemberRepository.existsByUserIdAndCommunityId(
                                        user.getId(), project.getCommunity().getId())))
                .orElse(false);
    }

    private Long currentViewerId() {
        return authUtil.getCurrentUserIdOptional().orElse(0L);
    }

    private ProjectResponseDto toProjectResponse(Project project) {
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

}
