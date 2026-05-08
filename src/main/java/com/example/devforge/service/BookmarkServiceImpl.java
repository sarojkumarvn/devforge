package com.example.devforge.service;

import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.example.devforge.dto.ProjectResponseDto;
import com.example.devforge.entity.BookMark;
import com.example.devforge.entity.Project;
import com.example.devforge.entity.User;
import com.example.devforge.repository.BookMarkRepository;
import com.example.devforge.repository.ProjectRepository;
import com.example.devforge.repository.UserRepository;
import com.example.devforge.security.AuthUtil;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookMarkService {

    private final BookMarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ModelMapper modelMapper;
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
                .map(b -> modelMapper.map(b.getProject(), ProjectResponseDto.class))
                .toList();
    }
}
