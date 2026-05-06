// CommunityService.java
package com.example.devforge.service;

import java.util.List;

import com.example.devforge.dto.CommunityRequestDto;
import com.example.devforge.dto.CommunityResponseDto;
import com.example.devforge.dto.ProjectResponseDto;
import com.example.devforge.dto.UserResponseDto;


public interface CommunityService {

    CommunityResponseDto createCommunity(Long userId, CommunityRequestDto dto);

    List<CommunityResponseDto> getAllCommunities();

    CommunityResponseDto getCommunityById(Long communityId);

    CommunityResponseDto updateCommunity(Long userId, Long communityId, CommunityRequestDto dto);

    void deleteCommunity(Long userId, Long communityId);

    String joinCommunity(Long userId, Long communityId);

    String leaveCommunity(Long userId, Long communityId);

    List<UserResponseDto> getAllMembers(Long communityId);

    List<ProjectResponseDto> getCommunityPosts(Long communityId, Long userId);
}