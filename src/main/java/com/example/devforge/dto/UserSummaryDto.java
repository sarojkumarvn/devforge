package com.example.devforge.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserSummaryDto {
    private Long id ;
    private String userName ;
    private String profilePictureUrl ;

}
