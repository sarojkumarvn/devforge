package com.example.devforge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.devforge.entity.Follow;

public interface FollowRepository extends JpaRepository<Follow , Long > {
    
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);   // it will check if they are exist or not 

    // To remove them 
    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);

    // To find by the follower id 
    List<Follow> findByFollowerId(Long followerId);

    // To find by the following id 
    List<Follow> findByFollowingId(Long followingId);

}
