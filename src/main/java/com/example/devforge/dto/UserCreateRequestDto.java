package com.example.devforge.dto;

import java.time.LocalDate;
import java.util.Set;

import com.example.devforge.entity.enums.Interest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateRequestDto {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @NotBlank
    @Size(min = 2, max = 20)
    private String userName;

    @Size(max = 500)
    private String profilePictureUrl;

    @Size(max = 500)
    private String coverPictureUrl;

    @Past
    private LocalDate dateOfBirth;

    private Boolean isPrivate;

    @Size(max = 500)
    private String bio;

    @Size(max = 120)
    private String location;

    @Size(max = 50)
    private Set<String> skills;

    @Size(max = 20)
    private Set<Interest> interests;
}
