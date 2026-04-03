package com.example.devforge.service;

import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.example.devforge.dto.ProjectResponseDto;
import com.example.devforge.entity.Like;
import com.example.devforge.entity.Project;
import com.example.devforge.entity.User;
import com.example.devforge.repository.LikeRepository;
import com.example.devforge.repository.ProjectRepository;
import com.example.devforge.repository.UserRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class LikeServiceImple implements LikeService {

    private final LikeRepository likeRepository ;
    private final UserRepository userRepository ;
    private final ProjectRepository projectRepository ;
    private final ModelMapper modelMapper ;

    @Override
    public void likeProject(Long userId, Long projectId) {
           if (likeRepository.findByUserIdAndProjectId(userId, projectId).isPresent()) {
            throw new RuntimeException("Already liked this project");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Like like = new Like();
        like.setUser(user);
        like.setProject(project);
        like.setCreatedAt(LocalDateTime.now());

        likeRepository.save(like);
        
    }

    @Override
    public void unlikeProject(Long userId, Long projectId) {
        Like like = likeRepository.findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new RuntimeException("Like not found"));

        likeRepository.delete(like);
    }

    @Override
    public List<ProjectResponseDto> getLikedProjectsLast90Days(Long userId) {
        LocalDateTime last90days = LocalDateTime.now().minusDays(90);


        return likeRepository
                .findByUserIdAndCreatedAtAfter(userId, last90days)
                .stream()
                .map(like -> modelMapper.map(like.getProject() , ProjectResponseDto.class)).toList(); 
                

    }
    

}
