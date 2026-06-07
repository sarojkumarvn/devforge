package com.example.devforge.repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.devforge.entity.Like;

public interface LikeRepository extends JpaRepository<Like , Long > {
    Optional<Like> findByUserIdAndProjectId(Long userId , long projectId ) ;

    List<Like> findByUserIdAndCreatedAtAfter (Long userId ,LocalDateTime date);

    @Query("SELECT l.project.id FROM Like l WHERE l.user.id = :userId AND l.project.id IN :projectIds")
    List<Long> findLikedProjectIds(Long userId, List<Long> projectIds);
}
