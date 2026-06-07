package com.example.devforge.service;

import java.util.List;


import com.example.devforge.dto.ProjectResponseDto;







public interface BookMarkService {

    void bookmarkProject(Long projectId) ;
    void removeBookmark(Long projectId);

    List<ProjectResponseDto> getBookmarkedProjectsLast90Days();
    

}
