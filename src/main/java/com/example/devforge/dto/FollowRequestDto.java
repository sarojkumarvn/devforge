package com.example.devforge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter

public class FollowRequestDto {
    private Long followerId ;

    @NotNull
    private Long followingId ;


}
