package com.example.devforge.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter

public class LoginResponseDto {
    String jwt  ;
    Long userId ;


}
