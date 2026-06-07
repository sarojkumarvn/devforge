package com.example.devforge.controller;


import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
import com.example.devforge.dto.ProjectCreateRequestDto;
import com.example.devforge.dto.ProjectResponseDto;
import com.example.devforge.dto.ProjectUpdateRequestDto;
import com.example.devforge.service.ProjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;



@RestController
@RequestMapping
@RequiredArgsConstructor
@Validated
public class ProjectController {

    private final ProjectService projectService;



    //  Create Project
    @PostMapping("/users/{userId}/projects")
    public ResponseEntity<ProjectResponseDto> createProject(
            @Positive @PathVariable Long userId,
            @Valid @RequestBody ProjectCreateRequestDto dto) {
                


        return ResponseEntity.ok(projectService.createProject(userId, dto));
    }

    //  Get Projects 
    @GetMapping("/projects")
    public ResponseEntity<Page<ProjectResponseDto>> getProjects(
            @RequestParam(required = false) String keyword,
            @Positive @RequestParam(required = false) Long userId,
            @Min(0) @RequestParam(defaultValue = "0") int page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        
        if (keyword != null) {
            return ResponseEntity.ok(projectService.searchProjects(keyword, page, size, sortBy, direction));
        }

        if (userId != null) {
            return ResponseEntity.ok(projectService.getProjectsByUser(userId, page, size, sortBy, direction));
        }

        return ResponseEntity.ok(projectService.getAllProjects(page, size, sortBy, direction));
    }


    @GetMapping("/projects/{projectId:\\d+}")
    public ResponseEntity<ProjectResponseDto> getProjectById(
            @Positive @PathVariable Long projectId) {

        return ResponseEntity.ok(projectService.getProjectById(projectId));
    }

    // Update Project
    @PutMapping("/users/{userId}/projects/{projectId}")
    public ResponseEntity<ProjectResponseDto> updateProject(
            @Positive @PathVariable Long userId,
            @Positive @PathVariable Long projectId,
            @Valid @RequestBody ProjectUpdateRequestDto dto) {

        return ResponseEntity.ok(projectService.updateProject(userId, projectId, dto));
    }

    //  Delete Project
    @DeleteMapping("/users/{userId}/projects/{projectId}")
    public ResponseEntity<ApiResponse> deleteProject(
            @Positive @PathVariable Long userId,
            @Positive @PathVariable Long projectId) {

        projectService.deleteProject(userId, projectId);
        return ResponseEntity.ok(new ApiResponse("Project deleted successfully"));
    }
}
