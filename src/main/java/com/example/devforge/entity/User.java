package com.example.devforge.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.example.devforge.entity.enums.Interest;
import com.example.devforge.entity.enums.LinkType;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @Email
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    @Size(min = 2 , max = 20)
    private String userName;

    @Column(nullable = true)
    private String profilePictureUrl;

    @Column(nullable = true, length = 500)
    private String bio;

    @Column(nullable = true)
    private String location;

    @Column(nullable = false)
    private Boolean isPrivate = false;

   
    @ElementCollection
    @CollectionTable(name = "user_skills", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill")
    private Set<String> skills;

 
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_interests", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "interest")
    private Set<Interest> interests;



    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> projects = new ArrayList<>();

 
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_links", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "link_type")
    private Set<LinkType> links;

    @Column(nullable = true)
    private LocalDate dateOfBirth;

  
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

  
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }




     public void addProject(Project project) {
        projects.add(project);
        project.setUser(this);
    }

    public void removeProject(Project project) {
        projects.remove(project);
        project.setUser(null);
    }
}