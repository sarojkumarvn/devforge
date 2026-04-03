package com.example.devforge.dto;

import java.util.Set;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ProjectRequestDto {
    private String title ;
    private String description ;
    private String githubLink ;
    private String liveDemoLink ;
    private Set<String> techStacks ;


}
