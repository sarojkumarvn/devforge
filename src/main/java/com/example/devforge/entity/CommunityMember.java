package com.example.devforge.entity;



import java.time.LocalDateTime;

import com.example.devforge.entity.enums.Role;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class CommunityMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;

    private Long userId ;


    @ManyToOne
    private Community community ;


    @Enumerated(EnumType.STRING)
    private Role role ;

    private LocalDateTime joinedAt ;

    

}
