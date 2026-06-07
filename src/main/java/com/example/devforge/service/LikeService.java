package com.example.devforge.service;

import java.util.List;

import com.example.devforge.dto.ProjectResponseDto;

public interface LikeService {

    void likeProject(Long projectId);
    void unlikeProject(Long projectId);

    List<ProjectResponseDto> getLikedProjectsLast90Days();
    


}
