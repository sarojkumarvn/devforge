package com.example.devforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.*;

@Data
@Getter
@Setter

public class CommentUpdateRequestDto {
    @NotBlank
    @Size(max = 1000)
    private String content;

}
