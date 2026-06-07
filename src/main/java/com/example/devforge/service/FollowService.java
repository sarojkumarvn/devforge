package com.example.devforge.service;

import org.springframework.data.domain.Page;
import com.example.devforge.dto.FollowRequestDto;
import com.example.devforge.dto.FollowResponseDto;

public interface FollowService  {
    FollowResponseDto followUser(FollowRequestDto dto) ;
    String unfollowUser (FollowRequestDto dto) ;
    Page<Long> getAllFollowers(Long userId, int page, int size) ;

    Page<Long> getAllFollowings(Long userId, int page, int size) ;
   

}
