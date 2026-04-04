package com.example.devforge.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.devforge.advice.ApiResponse;
import com.example.devforge.dto.ProjectRequestDto;
import com.example.devforge.dto.ProjectResponseDto;
import com.example.devforge.service.ProjectService;

import lombok.RequiredArgsConstructor;
/// TESTING DONE 
@RestController
@RequestMapping
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    //  Create Project
    @PostMapping("/users/{userId}/projects")
    public ResponseEntity<ProjectResponseDto> createProject(
            @PathVariable Long userId,
            @RequestBody ProjectRequestDto dto) {
                


        return ResponseEntity.ok(projectService.createProject(userId, dto));
    }

    //  Get Projects (ALL + SEARCH + PAGINATION)
    @GetMapping("/projects")
    public ResponseEntity<List<ProjectResponseDto>> getProjects(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // 🔥 Priority-based filtering
        if (keyword != null) {
            return ResponseEntity.ok(projectService.searchProjects(keyword, page, size));
        }

        if (userId != null) {
            return ResponseEntity.ok(projectService.getProjectsByUser(userId));
        }

        return ResponseEntity.ok(projectService.getAllProjects());
    }


    @GetMapping("/projects/{projectId:\\d+}")
    public ResponseEntity<ProjectResponseDto> getProjectById(
            @PathVariable Long projectId) {

        return ResponseEntity.ok(projectService.getProjectById(projectId));
    }

    // Update Project
    @PutMapping("/users/{userId}/projects/{projectId}")
    public ResponseEntity<ProjectResponseDto> updateProject(
            @PathVariable Long userId,
            @PathVariable Long projectId,
            @RequestBody ProjectRequestDto dto) {

        return ResponseEntity.ok(projectService.updateProject(userId, projectId, dto));
    }

    //  Delete Project
    @DeleteMapping("/users/{userId}/projects/{projectId}")
    public ResponseEntity<ApiResponse> deleteProject(
            @PathVariable Long userId,
            @PathVariable Long projectId) {

        projectService.deleteProject(userId, projectId);
        return ResponseEntity.ok(new ApiResponse("Project deleted successfully"));
    }
}