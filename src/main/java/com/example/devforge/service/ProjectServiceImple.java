package com.example.devforge.service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.management.RuntimeErrorException;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.devforge.dto.ProjectRequestDto;
import com.example.devforge.dto.ProjectResponseDto;
import com.example.devforge.entity.Project;
import com.example.devforge.entity.User;
import com.example.devforge.exception.ResourceNotFoundException;
import com.example.devforge.repository.ProjectRepository;
import com.example.devforge.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor

public class ProjectServiceImple implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public ProjectResponseDto createProject(Long userId, ProjectRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not available with the id : {}" + userId));
        Project project = modelMapper.map(dto, Project.class);
        Project saved = projectRepository.save(project);

        ProjectResponseDto response = modelMapper.map(saved, ProjectResponseDto.class);
        response.setUserId(user.getId());
        response.setUserName(user.getUserName());

        return response;
    }

    @Override
    public ProjectResponseDto updateProject(Long projectId, Long userId, ProjectRequestDto dto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with this id : {}" + projectId));

        if (!project.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not allowed to update this project..");

        }

        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setGithubLink(dto.getGithubLink());
        project.setLiveDemoLink(dto.getLiveDemoLink());
        project.setTechStacks(dto.getTechStacks());

        Project updated = projectRepository.save(project);

        ProjectResponseDto response = modelMapper.map(updated, ProjectResponseDto.class);

        response.setUserId(updated.getUser().getId());
        response.setUserName(updated.getUser().getUserName());

        return response;
    }

    @Override
    public void deleteProject(Long userId, Long projectId) {
        log.info("Deleting the project with the project ID :  {}" + projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with this id : {}" + projectId));

        if (!project.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not allowed to delete this project..");

        }
        projectRepository.delete(project);

    }

    @Override
    public ProjectResponseDto getProjectById(Long projectId) {
         Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with this id : {}" + projectId));
        ProjectResponseDto response = modelMapper.map(project , ProjectResponseDto.class);
        response.setUserId(project.getUser().getId());
        response.setUserName(project.getUser().getUserName());

        return response  ;
        
    }

    @Override
    public List<ProjectResponseDto> getAllProjects() {
        ProjectResponseDto response =  (ProjectResponseDto) projectRepository.findAll().stream()
                .map(project -> {
                    ProjectResponseDto dto = modelMapper.map(project, ProjectResponseDto.class);
                    dto.setUserId(project.getUser().getId());
                    dto.setUserName(project.getUser().getUserName());
                    return dto;
                })
                .toList();

        return (List<ProjectResponseDto>) response;
    }

@Override
public List<ProjectResponseDto> getProjectsByUser(Long userId) {

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    return projectRepository.findByUser(user)
            .stream()
            .map(project -> {
                ProjectResponseDto dto = modelMapper.map(project, ProjectResponseDto.class);
                dto.setUserId(user.getId());
                dto.setUserName(user.getUserName());
                return dto;
            })
            .toList();
}

@Override
public List<ProjectResponseDto> searchProjects(String keyword, int page, int size) {
    if(keyword == null || keyword.trim().isEmpty()) { // checking of the keyword is empty or what 
        throw new RuntimeException("Search keyword can not be empty");

    }

    Page<Project> projectPage = projectRepository.findByTitleContainingIgnoreCase(keyword, PageRequest.of(page, size));

    return projectPage.getContent()
    .stream()
    .map(project -> modelMapper.map(project , ProjectResponseDto.class))
    .toList() ;


}



}
