package com.example.devforge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.devforge.entity.Community;
import com.example.devforge.entity.User;

public interface CommunityRepository extends JpaRepository<Community , Long > {

    @Query( value = """
            SELECT u.* 
            FROM app_user u 
            JOIN community_member cm 
            ON u.id = cm.user_id 
            WHERE cm.community_id = :communityId
            """ , nativeQuery = true) 
    List<User> getAllMembers(Long communityId) ;


}
