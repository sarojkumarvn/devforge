package com.example.devforge.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "comments")

public class Comment {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    


    private String content;

    private LocalDateTime createdAt = LocalDateTime.now();

    // who wrote it 
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // which project 
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;


    // Parent comment (for replies)
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Comment parent;



    // One comment can have many replies 
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>();

}
