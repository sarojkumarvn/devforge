package com.example.devforge.dto;

import java.time.LocalDate;
import java.util.Set;

import com.example.devforge.entity.enums.Interest;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@Getter
@Setter

public class UserUpdateDto {

    private String userName ;
    private String bio;
    private String profilePictureUrl;
    private String location;
    private LocalDate dateOfBirth ;
    

    private Set<String> skills;
    private Set<Interest> interests;

}
