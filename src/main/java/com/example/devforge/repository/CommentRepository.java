package com.example.devforge.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.devforge.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment , Long > {

    @EntityGraph(attributePaths = {"user", "replies", "replies.user"})
    Page<Comment> findByProjectIdAndParentIsNull(Long projectId, Pageable pageable) ;
    

}
