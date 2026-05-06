package com.example.devforge.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.devforge.dto.LoginRequestDto;
import com.example.devforge.dto.LoginResponseDto;
import com.example.devforge.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager ;
    
public LoginResponseDto login(LoginRequestDto loginRequestDto) {
    
    // It will go to different authentication manager also the provider ( DAO ) then that will go through the  userService 
    // which we have implemented in the user entity 
      Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(), loginRequestDto.getPassword())

      );

      User user = (User)authentication.getPrincipal() ;


      // Now we have to generate tokens 

      return null ;
    }



}
