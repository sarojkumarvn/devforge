package com.example.devforge.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.devforge.dto.LoginRequestDto;
import com.example.devforge.dto.LoginResponseDto;
import com.example.devforge.dto.SignupResponseDto;
import com.example.devforge.dto.UserRequestDto;
import com.example.devforge.entity.User;
import com.example.devforge.repository.UserRepository;
import com.example.devforge.security.AuthUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final AuthenticationManager authenticationManager;
  private final AuthUtil authUtil;
  private final UserRepository userRepository ;
  private final PasswordEncoder passwordEncoder;

  // Login implementation

  public LoginResponseDto login(LoginRequestDto loginRequestDto) {

    // It will go to different authentication manager also the provider ( DAO ) then
    // that will go through the userService
    // which we have implemented in the user entity
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword())

    );

    User user = (User) authentication.getPrincipal();

    // Now we have to generate tokens
    String token = authUtil.generateAccessToken(user);

    return new LoginResponseDto(token, user.getId());
  }



  // Signup service implementation
  public SignupResponseDto signup(UserRequestDto signupRequestDto) {

    if (userRepository.findByUserName(signupRequestDto.getUserName()).isPresent()) {
      throw new IllegalArgumentException("Username already exists");
    }

    if (userRepository.findByEmail(signupRequestDto.getEmail()).isPresent()) {
      throw new IllegalArgumentException("Email already exists");
    }

    User user = User.builder()
        .email(signupRequestDto.getEmail())
        .userName(signupRequestDto.getUserName())
        .password(passwordEncoder.encode(signupRequestDto.getPassword()))
        .isPrivate(Boolean.TRUE.equals(signupRequestDto.getIsPrivate()))
        .build();

    user.setProfilePictureUrl(signupRequestDto.getProfilePictureUrl());
    user.setDateOfBirth(signupRequestDto.getDateOfBirth());
    user.setBio(signupRequestDto.getBio());
    user.setLocation(signupRequestDto.getLocation());
    if (signupRequestDto.getSkills() != null) {
      user.setSkills(signupRequestDto.getSkills());
    }
    if (signupRequestDto.getInterests() != null) {
      user.setInterests(signupRequestDto.getInterests());
    }

    User saved = userRepository.save(user);
    String token = authUtil.generateAccessToken(saved);

    return new SignupResponseDto(saved.getId(), saved.getUserName(), token);

  }

}
