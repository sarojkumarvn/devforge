package com.example.devforge.entity;



import java.time.LocalDateTime;

import com.example.devforge.entity.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Index;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    indexes = {
        @Index(name = "idx_community_member_community", columnList = "community_id"),
        @Index(name = "idx_community_member_user", columnList = "user_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "community_id"})
    }
)
@Getter
@Setter
public class CommunityMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;

    @Column(name = "user_id", nullable = false)
    private Long userId ;


    @ManyToOne
    @JoinColumn(name = "community_id", nullable = false)
    private Community community ;


    @Enumerated(EnumType.STRING)
    private Role role ;

    private LocalDateTime joinedAt ;

    

}
