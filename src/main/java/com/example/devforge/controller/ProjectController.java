package com.example.devforge.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.devforge.dto.ProjectRequestDto;
import com.example.devforge.dto.ProjectResponseDto;
import com.example.devforge.service.ProjectService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // Create Project
    @PostMapping("/users/{userId}/projects")
    public ResponseEntity<ProjectResponseDto> createProject(
            @PathVariable Long userId,
            @RequestBody ProjectRequestDto dto) {

        return ResponseEntity.ok(projectService.createProject(userId, dto));
    }

    // Get All Projects
    @GetMapping("/projects")
    public ResponseEntity<List<ProjectResponseDto>> getAllProjects() {

        return ResponseEntity.ok(projectService.getAllProjects());
    }

    // Get Project by ID
    @GetMapping("/projects/{projectId}")
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

    // Delete Project
    @DeleteMapping("/users/{userId}/projects/{projectId}")
    public ResponseEntity<String> deleteProject(
            @PathVariable Long userId,
            @PathVariable Long projectId) {

        projectService.deleteProject(userId, projectId);
        return ResponseEntity.ok("Project deleted successfully");
    }

    // Get Projects by User
    @GetMapping("/users/{userId}/projects")
    public ResponseEntity<List<ProjectResponseDto>> getProjectsByUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(projectService.getProjectsByUser(userId));
    }



    @GetMapping("/search")
    public ResponseEntity<List<ProjectResponseDto>> searchProjects (
        @RequestParam String keyword ,
        @RequestParam(defaultValue = "0") int page ,
        @RequestParam(defaultValue = "10") int size 
        )

        {
            return ResponseEntity.ok(projectService.searchProjects(keyword, page, size));
        }
}