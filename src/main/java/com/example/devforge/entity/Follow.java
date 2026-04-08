package com.example.devforge.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.ManyToAny;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_follow")
@Getter
@Setter
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id  ;


    @ManyToMany
    @JoinColumn(name = "follower_id" ,nullable = false)
    private User follower ;




    @ManyToMany
    @JoinColumn(name = "following_id" ,nullable = false)
    private User following ;

    private LocalDateTime createdAt ;






    


}
