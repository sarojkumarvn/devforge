package com.example.devforge.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.devforge.entity.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "community"})
    Page<Project> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "community"})
    @Query("""
        SELECT p FROM Project p
        WHERE p.isPublic = true
           OR :isAdmin = true
           OR p.user.id = :viewerId
           OR EXISTS (
                SELECT 1 FROM CommunityMember cm
                WHERE cm.community.id = p.community.id
                  AND cm.userId = :viewerId
           )
    """)
    Page<Project> findVisibleProjects(Long viewerId, boolean isAdmin, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "community"})
    Page<Project> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "community"})
    @Query("""
        SELECT p FROM Project p
        WHERE p.user.id = :userId
          AND (
                p.isPublic = true
             OR :isAdmin = true
             OR p.user.id = :viewerId
             OR EXISTS (
                    SELECT 1 FROM CommunityMember cm
                    WHERE cm.community.id = p.community.id
                      AND cm.userId = :viewerId
                )
          )
    """)
    Page<Project> findVisibleByUserId(Long userId, Long viewerId, boolean isAdmin, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "community"})
    Page<Project> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "community"})
    @Query("""
        SELECT p FROM Project p
        WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
          AND (
                p.isPublic = true
             OR :isAdmin = true
             OR p.user.id = :viewerId
             OR EXISTS (
                    SELECT 1 FROM CommunityMember cm
                    WHERE cm.community.id = p.community.id
                      AND cm.userId = :viewerId
                )
          )
    """)
    Page<Project> searchVisibleByTitle(String keyword, Long viewerId, boolean isAdmin, Pageable pageable);

     @EntityGraph(attributePaths = {"user", "community"})
     Page<Project> findAllByOrderByCreatedAtDesc(Pageable pageable);

      @EntityGraph(attributePaths = {"user", "community"})
      @Query("""
        SELECT p FROM Project p
        WHERE p.user.id IN :followingIds
    """)
    Page<Project> findFeedProjects(java.util.List<Long> followingIds, Pageable pageable);

      @EntityGraph(attributePaths = {"user", "community"})
      @Query("""
        SELECT p FROM Project p
        WHERE p.user.id IN (
            SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId
        )
          AND (
                p.isPublic = true
             OR :isAdmin = true
             OR p.user.id = :viewerId
             OR EXISTS (
                    SELECT 1 FROM CommunityMember cm
                    WHERE cm.community.id = p.community.id
                      AND cm.userId = :viewerId
                )
          )
    """)
    Page<Project> findFollowingFeedProjects(Long userId, Long viewerId, boolean isAdmin, Pageable pageable);
    

    @EntityGraph(attributePaths = {"user", "community"})
    @Query("""
    SELECT p FROM Project p
    WHERE p.isPublic = true
       OR :isAdmin = true
       OR p.user.id = :viewerId
       OR EXISTS (
            SELECT 1 FROM CommunityMember cm
            WHERE cm.community.id = p.community.id
              AND cm.userId = :viewerId
       )
""")
Page<Project> findPopularFeed(Long viewerId, boolean isAdmin, Pageable pageable);

@EntityGraph(attributePaths = {"user", "community"})
Page<Project> findByCommunityId(Long communityId, Pageable pageable);

}
