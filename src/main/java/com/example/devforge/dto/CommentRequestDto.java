package com.example.devforge.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CommentRequestDto {

    private Long userId ;
    private Long projectId ;
    private String content ;
    

}
