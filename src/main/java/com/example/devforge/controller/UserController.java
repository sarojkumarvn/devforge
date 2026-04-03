package com.example.devforge.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.devforge.dto.UserRequestDto;
import com.example.devforge.dto.UserResponseDto;
import com.example.devforge.dto.UserSummaryDto;
import com.example.devforge.dto.UserUpdateDto;

import com.example.devforge.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // create user 
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
        @RequestBody UserRequestDto dto
    ) {
        UserResponseDto response = userService.createUser(dto) ;
        return ResponseEntity.ok(response);





       
    }




    // Get user by id 
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUserById(
         @PathVariable Long userId
    )
     {
        UserResponseDto response = userService.getUserById(userId);
        return ResponseEntity.ok(response) ;

     }



    // Get all the users
@GetMapping
public ResponseEntity<List<UserSummaryDto>> getAllUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
) {
    return ResponseEntity.ok(userService.getAllUsers(page, size));
}

    // update the user details
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDto> updateUser (
        @PathVariable Long userId ,
        @RequestBody UserUpdateDto request
    ) 
     {
        UserResponseDto response = userService.updateUser(userId, request);

        return ResponseEntity.ok(response);


     }



    // delete  the user 
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long userId) {

        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }


}
