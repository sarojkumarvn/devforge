package com.example.devforge.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import com.example.devforge.dto.FeedResponseDto;
import com.example.devforge.dto.FollowResponseDto;
import com.example.devforge.entity.Project;
import com.example.devforge.entity.User;
import com.example.devforge.exception.ResourceNotFoundException;
import com.example.devforge.repository.FollowRepository;
import com.example.devforge.repository.ProjectRepository;
import com.example.devforge.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final ModelMapper modelMapper;

        private FeedResponseDto mapToDto(Project project, Long userId) {

        FeedResponseDto dto = modelMapper.map(project, FeedResponseDto.class);

        dto.setProjectId(project.getId());
        dto.setUserId(project.getUser().getId());
        dto.setUserName(project.getUser().getUserName());

       
        dto.setIsLiked(false);
        dto.setIsBookmarked(false);

        return dto;
    }

    @Override
    public Page<FeedResponseDto> getFeed(int page, int size, Long userId) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Project> projectPage = projectRepository.findAllByOrderByCreatedAtDesc(pageable);

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
                            .anyMatch(like -> like.getUser().getId().equals(userId)));

            dto.setIsBookmarked(
                    project.getBookmarks().stream()
                            .anyMatch(b -> b.getUser().getId().equals(userId)));

            return dto;
        });
    }

    @Override
    public Page<FeedResponseDto> getFollowingFeed(int page, int size, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found with this id : " + userId));
        List<Long> followingIds = followRepository.findFollowingIds(userId);

        if (followingIds.isEmpty()) {
            return Page.empty();
        }

        PageRequest pageable = PageRequest.of(page, size);

        Page<Project> project = projectRepository.findFeedProjects(followingIds, pageable);

        return project.map(p -> mapToDto(p, userId));

    }



    @Override
    public Page<FeedResponseDto> getPopularFeed(int page, int size, Long userId) {
        PageRequest pageable = PageRequest.of(page , size) ;
        Page<Project> projects = projectRepository.findPopularFeed(pageable);

        return projects.map(p -> mapToDto(p, userId));

        
    }
}