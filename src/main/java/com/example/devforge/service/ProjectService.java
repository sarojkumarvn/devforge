package com.example.devforge.service;

import java.util.List;

import com.example.devforge.dto.ProjectRequestDto;
import com.example.devforge.dto.ProjectResponseDto;

public interface ProjectService {
    ProjectResponseDto createProject(Long userId ,ProjectRequestDto dto) ; ///Create the project 

    ProjectResponseDto updateProject(Long projectId, Long userId , ProjectRequestDto dto) ; // update the project 

    void deleteProject(Long userId , Long projectId) ;  // service to delte the project 
    ProjectResponseDto getProjectById(Long projectId) ; // service to get the poject by the project id 

    List<ProjectResponseDto> getAllProjects() ; // here we can get all the projects 
     // get the projects by the user id 

    List<ProjectResponseDto> getProjectsByUser(Long userId);
    

   

    

}
