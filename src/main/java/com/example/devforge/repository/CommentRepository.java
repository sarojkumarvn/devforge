package com.example.devforge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.devforge.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment , Long > {

    List<Comment> findByProjectIdAndParentIsNull(Long projectId) ;
    

}
