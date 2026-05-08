package com.example.devforge.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.example.devforge.dto.CommunityRequestDto;
import com.example.devforge.dto.CommunityResponseDto;
import com.example.devforge.dto.ProjectResponseDto;
import com.example.devforge.dto.UserResponseDto;
import com.example.devforge.entity.Community;
import com.example.devforge.entity.CommunityMember;
import com.example.devforge.entity.Project;
import com.example.devforge.entity.User;
import com.example.devforge.entity.enums.Role;
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
    private final ModelMapper modelMapper;
    private final CommunityMemberRepository communityMemberRepository;
    private final ProjectRepository projectRepository ;
    private final AuthUtil authUtil;

    @Override

public CommunityResponseDto createCommunity(Long userId, CommunityRequestDto dto) {

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

    return modelMapper.map(savedCommunity, CommunityResponseDto.class);
}

    @Override
    public List<CommunityResponseDto> getAllCommunities() {

        return communityRepository.findAll()
                .stream()
                .map(community -> modelMapper.map(community, CommunityResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public CommunityResponseDto getCommunityById(Long communityId) {

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));

        return modelMapper.map(community, CommunityResponseDto.class);
    }

    @Override
    public CommunityResponseDto updateCommunity(Long userId, Long communityId, CommunityRequestDto dto) {
        authUtil.requireCurrentUser(userId);
        requireCommunityAdmin(userId, communityId);

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));

        community.setName(dto.getName());
        community.setDescription(dto.getDescription());
        community.setLogoUrl(dto.getLogoUrl());
        community.setBannerUrl(dto.getBannerUrl());
        community.setPrivacy(dto.getPrivacy());

        Community updatedCommunity = communityRepository.save(community);

        return modelMapper.map(updatedCommunity, CommunityResponseDto.class);
    }

    @Override
    public void deleteCommunity(Long userId, Long communityId) {
        authUtil.requireCurrentUser(userId);
        requireCommunityAdmin(userId, communityId);

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));

        communityRepository.delete(community);
    }

    @Override
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
    public List<UserResponseDto> getAllMembers(Long communityId) {

        communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community Not found with this id " + communityId));

        List<CommunityMember> members = communityMemberRepository.findByCommunityId(communityId);
        return members.stream()
                .map(member -> userRepository.findById(member.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found")))
                .map(user -> modelMapper.map(user, UserResponseDto.class)).toList();
    }

    @Override
    public List<ProjectResponseDto> getCommunityPosts(Long communityId, Long userId) {
        authUtil.requireCurrentUser(userId);
         communityRepository.findById(communityId).orElseThrow(
            () ->  new ResourceNotFoundException("Community Not found")


        );

        boolean isMember = communityMemberRepository.existsByUserIdAndCommunityId(userId, communityId);

        if (!isMember) {
            throw new RuntimeException("You have to join this community first !") ;
        } 

        List<Project> projects = projectRepository.findByCommunityIdOrderByCreatedAtDesc(communityId);
        return projects.stream()
        .map(project -> modelMapper.map(project , ProjectResponseDto.class)).toList() ;
        
    }

    private void requireCommunityAdmin(Long userId, Long communityId) {
        CommunityMember member = communityMemberRepository.findByUserIdAndCommunityId(userId, communityId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this community"));

        if (member.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only community admins can perform this action");
        }
    }

}
