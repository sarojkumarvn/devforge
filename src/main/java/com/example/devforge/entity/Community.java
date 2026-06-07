package com.example.devforge.entity;


import java.time.LocalDateTime;
import java.util.List;

import com.example.devforge.entity.enums.PrivacyType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "community",
    indexes = {
        @Index(name = "idx_community_name", columnList = "community_name"),
        @Index(name = "idx_community_privacy", columnList = "privacy")
    }
)
public class Community {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "community_name")
    private String name;

    private String description;

    private String logoUrl;

    private String bannerUrl;

    @Enumerated(EnumType.STRING)
    private PrivacyType privacy;


    @Transient
    private List<User> members ;



    private LocalDateTime CreatedAt ;

}
