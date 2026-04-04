package com.example.devforge.repository;

import java.util.List;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
    

}