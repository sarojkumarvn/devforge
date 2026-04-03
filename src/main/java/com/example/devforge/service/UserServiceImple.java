package com.example.devforge.service;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.devforge.dto.UserRequestDto;
import com.example.devforge.dto.UserResponseDto;
import com.example.devforge.dto.UserSummaryDto;
import com.example.devforge.dto.UserUpdateDto;
import com.example.devforge.entity.User;
import com.example.devforge.exception.ResourceNotFoundException;
import com.example.devforge.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Service
@Slf4j
@RequiredArgsConstructor
@Transactional

public class UserServiceImple implements UserService {

    private final UserRepository userRepository ;
    private final ModelMapper modelMapper ;
  

   
    

    @Override
    public UserResponseDto createUser(UserRequestDto dto) {
        if(userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        if(userRepository.findByUserName(dto.getUserName()).isPresent()) {
            throw new RuntimeException("This username is already exists");

        }

        User user = new User() ;
        user.setEmail(dto.getEmail());
        user.setBio(dto.getBio());
        user.setUserName(dto.getUserName());
        user.setPassword(dto.getPassword());
        user.setBio(dto.getBio());
        user.setLocation(dto.getLocation());
        user.setSkills(dto.getSkills());
        user.setInterests(dto.getInterests());

        User savedUser = userRepository.save(user);


        UserResponseDto response = modelMapper.map(savedUser , UserResponseDto.class) ;

        return response ;
    }

   @Override
    public UserResponseDto getUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User not found with this id : {}" + userId));

        return null ;
    }

    //   @Override
    // public List<UserSummaryDto> getAllUsers() {
    //     return userRepository.findAll()
    //             .stream()
    //             // .map(this::mapToSummaryDto)
    //             .toList();
    // }

    @Override
    public UserResponseDto updateUser(Long userId, UserUpdateDto dto) {
       return null ;
    }

    @Override
    public void deleteUser(Long userId) {
       
    }

    @Override
    public List<UserSummaryDto> getAllUsers() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllUsers'");
    }

}
