package com.example.devforge.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Getter
@Setter

public class CommentResponseDto {

    private Long id;
    private String content;

    

    private String userName;
    private LocalDateTime createdAt;
    private Long projectId ;
  
    private List<CommentResponseDto> replies;

}
