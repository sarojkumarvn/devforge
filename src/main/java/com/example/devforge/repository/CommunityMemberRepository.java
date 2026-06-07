package com.example.devforge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.devforge.entity.CommunityMember;

public interface CommunityMemberRepository extends JpaRepository<CommunityMember , Long >  {
    List<CommunityMember> findByCommunityId(Long communityId);

    Page<CommunityMember> findByCommunityId(Long communityId, Pageable pageable);

    Optional<CommunityMember> findByUserIdAndCommunityId(
            Long userId,
            Long communityId
    );

    boolean existsByUserIdAndCommunityId(
            Long userId,
            Long communityId
    );

    void deleteByUserIdAndCommunityId(Long userId, Long communityId);

}
