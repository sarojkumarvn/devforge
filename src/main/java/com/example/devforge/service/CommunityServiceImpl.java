package com.example.devforge.service;

import java.time.LocalDateTime;
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

import com.example.devforge.dto.CommunityCreateRequestDto;
import com.example.devforge.dto.CommunityResponseDto;
import com.example.devforge.dto.CommunityUpdateRequestDto;
import com.example.devforge.dto.ProjectResponseDto;
import com.example.devforge.dto.UserResponseDto;
import com.example.devforge.entity.Community;
import com.example.devforge.entity.CommunityMember;
import com.example.devforge.entity.Project;
import com.example.devforge.entity.User;
import com.example.devforge.entity.enums.PrivacyType;
import com.example.devforge.entity.enums.Role;
import com.example.devforge.exception.ForbiddenException;
import com.example.devforge.exception.ResourceNotFoundException;
import com.example.devforge.repository.CommunityMemberRepository;
import com.example.devforge.repository.CommunityRepository;
import com.example.devforge.repository.ProjectRepository;
import com.example.devforge.repository.UserRepository;
import com.example.devforge.security.AuthUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CommunityServiceImpl implements CommunityService {

    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final ProjectRepository projectRepository ;
    private final AuthUtil authUtil;

    @Override
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @CacheEvict(cacheNames = {"communityPages", "communities"}, allEntries = true)
public CommunityResponseDto createCommunity(Long userId, CommunityCreateRequestDto dto) {

    authUtil.requireCurrentUser(userId);

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Community community = new Community();

    community.setName(dto.getName());
    community.setDescription(dto.getDescription());
    community.setLogoUrl(dto.getLogoUrl());
    community.setBannerUrl(dto.getBannerUrl());
    community.setPrivacy(dto.getPrivacy());

    community.setCreatedAt(LocalDateTime.now());

    // If Community entity has owner/creator field
    // community.setOwner(user);

    Community savedCommunity = communityRepository.save(community);

    CommunityMember owner = new CommunityMember();
    owner.setUserId(userId);
    owner.setCommunity(savedCommunity);
    owner.setRole(Role.ADMIN);
    owner.setJoinedAt(LocalDateTime.now());

    communityMemberRepository.save(owner);

    return toCommunityResponse(savedCommunity);
}

    @Override
    public Page<CommunityResponseDto> getAllCommunities(int page, int size, String sortBy, String direction) {
        Pageable pageable = buildPageRequest(page, size, sortBy, direction, allowedCommunitySorts(), "id");

        Page<Community> communities = communityRepository.findVisibleCommunities(
                authUtil.getCurrentUserIdOptional().orElse(0L),
                authUtil.isAdmin(),
                pageable);

        List<CommunityResponseDto> content = communities.getContent().stream()
                .map(this::toCommunityResponse)
                .toList();

        return new PageImpl<>(content, pageable, communities.getTotalElements());
    }

    @Override
    @Cacheable(cacheNames = "communities", key = "#communityId + ':viewer:' + @authUtil.getCurrentCacheUserId()")
    public CommunityResponseDto getCommunityById(Long communityId) {

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));

        requireCanViewCommunity(community);

        return toCommunityResponse(community);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @CachePut(cacheNames = "communities", key = "#communityId + ':viewer:' + @authUtil.getCurrentCacheUserId()")
    @CacheEvict(cacheNames = {"communityPages", "communityMembers", "communityPosts"}, allEntries = true)
    public CommunityResponseDto updateCommunity(Long userId, Long communityId, CommunityUpdateRequestDto dto) {
        authUtil.requireCurrentUserOrAdmin(userId);
        requireCommunityAdmin(userId, communityId);

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));

        community.setName(dto.getName());
        community.setDescription(dto.getDescription());
        community.setLogoUrl(dto.getLogoUrl());
        community.setBannerUrl(dto.getBannerUrl());
        community.setPrivacy(dto.getPrivacy());

        Community updatedCommunity = communityRepository.save(community);

        return toCommunityResponse(updatedCommunity);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @CacheEvict(cacheNames = {"communities", "communityPages", "communityMembers", "communityPosts", "projectPages", "feedPages"}, allEntries = true)
    public void deleteCommunity(Long userId, Long communityId) {
        authUtil.requireCurrentUserOrAdmin(userId);
        requireCommunityAdmin(userId, communityId);

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));

        communityRepository.delete(community);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @CacheEvict(cacheNames = {"communities", "communityPages", "communityMembers", "communityPosts", "projectPages", "feedPages"}, allEntries = true)
    public String joinCommunity(Long userId, Long communityId) {
        authUtil.requireCurrentUser(userId);

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));

        if (communityMemberRepository.existsByUserIdAndCommunityId(userId, communityId)) {
            return "Already joined this community";
        }

        CommunityMember member = new CommunityMember();

        member.setUserId(userId);
        member.setCommunity(community);
        member.setRole(Role.USER);
        member.setJoinedAt(LocalDateTime.now());

        communityMemberRepository.save(member);

        return "Joined community successfully";
    }

    @Override
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @CacheEvict(cacheNames = {"communities", "communityPages", "communityMembers", "communityPosts", "projectPages", "feedPages"}, allEntries = true)
    public String leaveCommunity(Long userId, Long communityId) {
        authUtil.requireCurrentUser(userId);

        communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));

        CommunityMember member = communityMemberRepository.findByUserIdAndCommunityId(userId, communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community membership not found"));

        if (member.getRole() == Role.ADMIN) {
            throw new AccessDeniedException("Community admin cannot leave before transferring ownership");
        }

        communityMemberRepository.deleteByUserIdAndCommunityId(userId, communityId);

        return "Left community successfully";
    }

    @Override
    public Page<UserResponseDto> getAllMembers(Long communityId, int page, int size) {

        communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community Not found with this id " + communityId));
        requireCanViewCommunity(communityId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "joinedAt"));
        Page<CommunityMember> members = communityMemberRepository.findByCommunityId(communityId, pageable);
        List<UserResponseDto> content = members.getContent().stream()
                .map(member -> userRepository.findById(member.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found")))
                .map(this::toUserResponse).toList();

        return new PageImpl<>(content, pageable, members.getTotalElements());
    }

    @Override
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Page<ProjectResponseDto> getCommunityPosts(Long communityId, int page, int size, String sortBy, String direction) {
        Long userId = authUtil.getCurrentUserId();
         communityRepository.findById(communityId).orElseThrow(
            () ->  new ResourceNotFoundException("Community Not found")


        );

        boolean isMember = communityMemberRepository.existsByUserIdAndCommunityId(userId, communityId);

        if (!isMember) {
            throw new ForbiddenException("You have to join this community first") ;
        } 

        Pageable pageable = buildPageRequest(page, size, sortBy, direction, allowedProjectSorts(), "createdAt");
        Page<Project> projects = projectRepository.findByCommunityId(communityId, pageable);
        List<ProjectResponseDto> content = projects.getContent().stream()
        .map(this::toProjectResponse).toList() ;

        return new PageImpl<>(content, pageable, projects.getTotalElements());
        
    }

    private void requireCommunityAdmin(Long userId, Long communityId) {
        if (authUtil.isAdmin()) {
            return;
        }

        CommunityMember member = communityMemberRepository.findByUserIdAndCommunityId(userId, communityId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this community"));

        if (member.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only community admins can perform this action");
        }
    }

    private void requireCanViewCommunity(Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community Not found with this id " + communityId));
        requireCanViewCommunity(community);
    }

    private void requireCanViewCommunity(Community community) {
        if (!canViewCommunity(community)) {
            throw new AccessDeniedException("Forbidden");
        }
    }

    private boolean canViewCommunity(Community community) {
        if (community.getPrivacy() != PrivacyType.PRIVATE) {
            return true;
        }

        return authUtil.getCurrentUserOptional()
                .map(user -> user.getRole() == Role.ADMIN
                        || communityMemberRepository.existsByUserIdAndCommunityId(user.getId(), community.getId()))
                .orElse(false);
    }

    private PageRequest buildPageRequest(
            int page,
            int size,
            String sortBy,
            String direction,
            Set<String> allowedSorts,
            String fallbackSort) {
        String property = allowedSorts.contains(sortBy) ? sortBy : fallbackSort;
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(sortDirection, property));
    }

    private Set<String> allowedCommunitySorts() {
        return Set.of("id", "name", "privacy");
    }

    private Set<String> allowedProjectSorts() {
        return Set.of("createdAt", "updatedAt", "title", "score", "likeCount", "commentCount", "bookmarkCount");
    }

    private CommunityResponseDto toCommunityResponse(Community community) {
        CommunityResponseDto dto = new CommunityResponseDto();
        dto.setId(community.getId());
        dto.setName(community.getName());
        dto.setDescription(community.getDescription());
        dto.setLogoUrl(community.getLogoUrl());
        dto.setBannerUrl(community.getBannerUrl());
        dto.setPrivacy(community.getPrivacy());
        return dto;
    }

    private UserResponseDto toUserResponse(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUserName(user.getUserName());
        dto.setBio(user.getBio());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        dto.setLocation(user.getLocation());
        dto.setSkills(user.getSkills());
        dto.setInterests(user.getInterests());
        dto.setFollowerCount(user.getFollowerCount());
        dto.setFollowingCount(user.getFollowingCount());
        return dto;
    }

    private ProjectResponseDto toProjectResponse(Project project) {
        ProjectResponseDto dto = new ProjectResponseDto();
        dto.setId(project.getId());
        dto.setTitle(project.getTitle());
        dto.setDescription(project.getDescription());
        dto.setGithubLink(project.getGithubLink());
        dto.setLiveDemoLink(project.getLiveDemoLink());
        dto.setTechStacks(project.getTechStacks());
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
