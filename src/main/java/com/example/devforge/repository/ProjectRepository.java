package com.example.devforge.repository;

import java.util.List;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.devforge.entity.Project;
import com.example.devforge.entity.User;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {


    List<Project> findByUser(User user);
    Page<Project>
     findByTitleContainingIgnoreCase
     (String keyword , PageRequest pageable);

     Page<Project> findAllByOrderByCreatedAtDesc(Pageable pageable);

      @Query("""
        SELECT p FROM Project p
        WHERE p.user.id IN :followingIds
        ORDER BY p.createdAt DESC
    """)
    Page<Project> findFeedProjects(List<Long> followingIds, Pageable pageable);
    

    @Query("""
    SELECT p FROM Project p
    ORDER BY p.score DESC
""")
Page<Project> findPopularFeed(Pageable pageable);

}