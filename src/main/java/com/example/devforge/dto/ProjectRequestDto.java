package com.example.devforge.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ProjectRequestDto {
    
    private Long userId ;

    @NotBlank
    @Size(max = 255)
    private String title ;

    @NotBlank
    @Size(max = 1290)
    private String description ;

    @Size(max = 500)
    private String githubLink ;

    @Size(max = 500)
    private String liveDemoLink ;

    @Size(max = 10)
    private String[] photos ;

    @Size(max = 30)
    private Set<String> techStacks ;

    private Boolean isPublic  ;
    private Long communityId ;


}
