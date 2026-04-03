package com.example.devforge.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.devforge.entity.BookMark;

public interface BookMarkRepository  extends JpaRepository<BookMark , Long>
{
    Optional<BookMark> findByUserIdAndProjectId(Long userId , Long projectId) ;
     List<BookMark> findByUserIdAndCreatedAtAfter(Long userId, LocalDateTime date);


}
