package com.example.devforge.dto;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
public class FollowResponseDto {
    private Long followingId ;
    private Long followerId ;
    private String message ;


}
