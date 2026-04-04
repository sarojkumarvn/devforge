package com.example.devforge.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class FeedResponseDto {
    private Long projectId ;
    private String title;
    private String description;
    private String[] photos ;

    private Long userId;
    private String userName;

    private Long likeCount;
    private Long commentCount;

    private Boolean isLiked;
    private Boolean isBookmarked;

    private LocalDateTime createdAt;


}
