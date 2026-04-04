package com.example.devforge.service;



import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import com.example.devforge.dto.FeedResponseDto;
import com.example.devforge.entity.Project;
import com.example.devforge.repository.ProjectRepository;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final ProjectRepository projectRepository;

    @Override
    public Page<FeedResponseDto> getFeed(int page, int size, Long userId) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Project> projectPage =
                projectRepository.findAllByOrderByCreatedAtDesc(pageable);

        return projectPage.map(project -> {
            FeedResponseDto dto = new FeedResponseDto();

            dto.setProjectId(project.getId());
            dto.setTitle(project.getTitle());
            dto.setDescription(project.getDescription());

            dto.setUserId(project.getUser().getId());
            dto.setUserName(project.getUser().getUserName());

            dto.setCreatedAt(project.getCreatedAt());

          
            dto.setLikeCount((long) project.getLikes().size());
            dto.setCommentCount((long) project.getComments().size());

            dto.setIsLiked(
                project.getLikes().stream()
                    .anyMatch(like -> like.getUser().getId().equals(userId))
            );

            dto.setIsBookmarked(
                project.getBookmarks().stream()
                    .anyMatch(b -> b.getUser().getId().equals(userId))
            );

            return dto;
        });
    }
}