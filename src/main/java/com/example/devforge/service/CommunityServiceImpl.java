package com.example.devforge.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.example.devforge.dto.CommunityRequestDto;
import com.example.devforge.dto.CommunityResponseDto;
import com.example.devforge.dto.UserResponseDto;
import com.example.devforge.entity.Community;
import com.example.devforge.entity.CommunityMember;
import com.example.devforge.entity.enums.Role;
import com.example.devforge.exception.ResourceNotFoundException;
import com.example.devforge.repository.CommunityMemberRepository;
import com.example.devforge.repository.CommunityRepository;
import com.example.devforge.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final ModelMapper modelMapper;
    private final CommunityMemberRepository communityMemberRepository;

    @Override
    public CommunityResponseDto createCommunity(Long userId, CommunityRequestDto dto) {

        Community community = modelMapper.map(dto, Community.class); // dto --> Community

        Community savedCommunity = communityRepository.save(community); // saved the community

        return modelMapper.map(savedCommunity, CommunityResponseDto.class); // savedCommunity ----> Response class
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

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));

        communityRepository.delete(community);
    }

    @Override
    public String joinCommunity(Long userId, Long communityId) {

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

        communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));

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

}