package com.example.devforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CommentRequestDto {

    @NotNull
    private Long projectId ;

    @NotBlank
    @Size(max = 1000)
    private String content ;
    

}
