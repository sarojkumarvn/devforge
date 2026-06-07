package com.example.devforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ReplyRequestDto {

    @NotBlank
    @Size(max = 1000)
    private String content;

    private Long parentId ;
}
