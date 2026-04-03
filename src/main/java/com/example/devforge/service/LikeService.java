package com.example.devforge.service;

import java.util.List;

import com.example.devforge.dto.ProjectResponseDto;

public interface LikeService {

    void likeProject(Long userId , Long projectId);
    void unlikeProject(Long userId, Long projectId);

    List<ProjectResponseDto> getLikedProjectsLast90Days(Long userId);


}
