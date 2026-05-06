package com.example.devforge.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class AuthUtil {
    @Value("${jwt.secretKey}")
    private String jwttoken ;

}
