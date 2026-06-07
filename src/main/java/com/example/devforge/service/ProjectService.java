package com.example.devforge.service;

import org.springframework.data.domain.Page;
import com.example.devforge.dto.ProjectCreateRequestDto;
import com.example.devforge.dto.ProjectResponseDto;
import com.example.devforge.dto.ProjectUpdateRequestDto;

public interface ProjectService {
    ProjectResponseDto createProject(Long userId ,ProjectCreateRequestDto dto) ; ///Create the project 

    ProjectResponseDto updateProject(Long userId, Long projectId , ProjectUpdateRequestDto dto) ; // update the project 

    void deleteProject(Long userId , Long projectId) ;  // service to delte the project 
    ProjectResponseDto getProjectById(Long projectId) ; // service to get the poject by the project id 

    Page<ProjectResponseDto> getAllProjects(int page, int size, String sortBy, String direction) ; // here we can get all the projects 
     // get the projects by the user id 

    Page<ProjectResponseDto> getProjectsByUser(Long userId, int page, int size, String sortBy, String direction);

    Page<ProjectResponseDto> searchProjects(String keyword , int page , int size, String sortBy, String direction);
    

   

    

}
