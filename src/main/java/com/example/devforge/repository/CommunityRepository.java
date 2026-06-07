package com.example.devforge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import com.example.devforge.entity.Community;

public interface CommunityRepository extends JpaRepository<Community , Long > {

    @Query("""
        SELECT c FROM Community c
        WHERE c.privacy <> com.example.devforge.entity.enums.PrivacyType.PRIVATE
           OR :isAdmin = true
           OR EXISTS (
                SELECT 1 FROM CommunityMember cm
                WHERE cm.community.id = c.id
                  AND cm.userId = :viewerId
           )
    """)
    Page<Community> findVisibleCommunities(Long viewerId, boolean isAdmin, Pageable pageable);

}
