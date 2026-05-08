package com.example.devforge.dto;

import java.time.LocalDateTime;
import java.util.Set;

import com.example.devforge.entity.enums.ProjectStatus;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ProjectResponseDto {

    private Long id;

    private String title;
    private String description;

    private String githubLink;
    private String liveDemoLink;

    private Set<String> techStacks;

    private ProjectStatus status;

    private String[] photos;

    private Long userId;
    private String userName;

    private LocalDateTime createdAt;

    // ADD THESE
    private Boolean isPublic;

    private Long likeCount;
    private Long commentCount;
    private Long bookmarkCount;

    private Long communityId;
}