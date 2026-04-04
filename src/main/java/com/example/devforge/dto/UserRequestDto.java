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

public class UserRequestDto {

    private String email;
    private String password;
    private String userName;
    private String profilePictureUrl;
    private LocalDate dateOfBirth;
    private Boolean isPrivate;

    private String bio;
    private String location;

    private Set<String> skills;
    private Set<Interest> interests;

}
