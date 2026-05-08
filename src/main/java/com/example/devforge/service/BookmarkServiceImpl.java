package com.example.devforge.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.devforge.dto.ProjectResponseDto;
import com.example.devforge.entity.BookMark;
import com.example.devforge.entity.Project;
import com.example.devforge.entity.User;
import com.example.devforge.repository.BookMarkRepository;
import com.example.devforge.repository.ProjectRepository;
import com.example.devforge.repository.UserRepository;
import com.example.devforge.security.AuthUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookMarkService {

    private final BookMarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final AuthUtil authUtil;

    @Override
    public void bookmarkProject(Long userId, Long projectId) {

        authUtil.requireCurrentUser(userId);

        if (bookmarkRepository.findByUserIdAndProjectId(userId, projectId).isPresent()) {
            throw new RuntimeException("Already bookmarked");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        BookMark bookmark = new BookMark();

        bookmark.setUser(user);
        bookmark.setProject(project);
        bookmark.setCreatedAt(LocalDateTime.now());

        project.incrementBookmarkCount();

        bookmarkRepository.save(bookmark);
        projectRepository.save(project);
    }

    @Override
    public void removeBookmark(Long userId, Long projectId) {

        authUtil.requireCurrentUser(userId);

        BookMark bookmark = bookmarkRepository
                .findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new RuntimeException("Bookmark not found"));

        bookmark.getProject().decrementBookmarkCount();

        bookmarkRepository.delete(bookmark);

        projectRepository.save(bookmark.getProject());
    }

    @Override
    public List<ProjectResponseDto> getBookmarkedProjectsLast90Days(Long userId) {

        authUtil.requireCurrentUser(userId);

        LocalDateTime last90Days = LocalDateTime.now().minusDays(90);

        return bookmarkRepository
                .findByUserIdAndCreatedAtAfter(userId, last90Days)
                .stream()
                .map(bookmark -> mapToResponse(bookmark.getProject()))
                .toList();
    }

    private ProjectResponseDto mapToResponse(Project project) {

        ProjectResponseDto dto = new ProjectResponseDto();

        dto.setId(project.getId());
        dto.setTitle(project.getTitle());
        dto.setDescription(project.getDescription());

        dto.setGithubLink(project.getGithubLink());
        dto.setLiveDemoLink(project.getLiveDemoLink());

        dto.setIsPublic(project.getIsPublic());

        dto.setPhotos(List.copyOf(project.getPhotos()).toArray(new String[0]));
        dto.setTechStacks(Set.copyOf(project.getTechStacks()));

        dto.setLikeCount(project.getLikeCount());
        dto.setCommentCount(project.getCommentCount());
        dto.setBookmarkCount(project.getBookmarkCount());

        dto.setCreatedAt(project.getCreatedAt());

        dto.setUserId(project.getUser().getId());
        dto.setUserName(project.getUser().getUserName());

        if (project.getCommunity() != null) {
            dto.setCommunityId(project.getCommunity().getId());
        }

        return dto;
    }
}