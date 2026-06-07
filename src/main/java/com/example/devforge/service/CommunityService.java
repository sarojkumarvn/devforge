// CommunityService.java
package com.example.devforge.service;

import org.springframework.data.domain.Page;
import com.example.devforge.dto.CommunityCreateRequestDto;
import com.example.devforge.dto.CommunityResponseDto;
import com.example.devforge.dto.CommunityUpdateRequestDto;
import com.example.devforge.dto.ProjectResponseDto;
import com.example.devforge.dto.UserResponseDto;


public interface CommunityService {

    CommunityResponseDto createCommunity(Long userId, CommunityCreateRequestDto dto);

    Page<CommunityResponseDto> getAllCommunities(int page, int size, String sortBy, String direction);

    CommunityResponseDto getCommunityById(Long communityId);

    CommunityResponseDto updateCommunity(Long userId, Long communityId, CommunityUpdateRequestDto dto);

    void deleteCommunity(Long userId, Long communityId);

    String joinCommunity(Long userId, Long communityId);

    String leaveCommunity(Long userId, Long communityId);

    Page<UserResponseDto> getAllMembers(Long communityId, int page, int size);

    Page<ProjectResponseDto> getCommunityPosts(Long communityId, int page, int size, String sortBy, String direction);
}
