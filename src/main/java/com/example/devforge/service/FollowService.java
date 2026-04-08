package com.example.devforge.service;

import java.util.List;

import com.example.devforge.dto.FollowRequestDto;
import com.example.devforge.dto.FollowResponseDto;

public interface FollowService  {
    FollowResponseDto followUser(FollowRequestDto dto) ;
    String unfollowUser (FollowRequestDto dto) ;
    List <Long> getAllFollowers (Long userId) ;

    List<Long> getAllFollowings(Long userId) ;
   

}
