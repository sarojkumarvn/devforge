package com.example.devforge.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter

public class FollowRequestDto {
    private Long followerId ;
    private Long followingId ;


}
