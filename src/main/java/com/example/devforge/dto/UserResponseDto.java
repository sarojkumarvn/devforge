package com.example.devforge.dto;

import java.util.Set;

import com.example.devforge.entity.enums.Interest;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@Getter
@Setter

public class UserResponseDto {
    private Long id;
    private String userName;
    private String bio;
    private String profilePictureUrl;
    private String location;

    private Set<String> skills;
    private Set<Interest> interests;

    private Long followerCount;
    private Long followingCount;

}
