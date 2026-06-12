package com.example.devforge.dto;

import java.time.LocalDate;
import java.util.Set;

import com.example.devforge.entity.enums.Interest;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@Getter
@Setter

public class UserUpdateDto {

    @Size(min = 2, max = 20)
    private String userName ;

    @Size(max = 500)
    private String bio;

    @Size(max = 500)
    private String profilePictureUrl;

    @Size(max = 500)
    private String coverPictureUrl;

    @Size(max = 120)
    private String location;

    @Past
    private LocalDate dateOfBirth ;
    

    @Size(max = 50)
    private Set<String> skills;

    @Size(max = 20)
    private Set<Interest> interests;

}
