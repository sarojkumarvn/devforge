package com.example.devforge.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.devforge.dto.FeedResponseDto;
import com.example.devforge.service.FeedService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;


//TESTED
@RestController
@RequestMapping("/feed")
@RequiredArgsConstructor
@Validated
public class FeedController {
    private final FeedService feedService ;

    @GetMapping
    public ResponseEntity<Page<FeedResponseDto>> getFeeds (
        @Min(0) @RequestParam(defaultValue = "0") int page ,
        @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size ,
        @Positive @RequestParam(required = false) Long userId
    ) {
        return ResponseEntity.ok(
            feedService.getFeed(page, size)
        ) ;
    }




    @GetMapping("/following") 
    public ResponseEntity<Page<FeedResponseDto>> getFollowingFeeds (
        @Positive @RequestParam(required = false) Long userId ,
        @Min(0) @RequestParam(defaultValue = "0") int page ,
        @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
            feedService.getFollowingFeed(page, size)
        ) ;
    }




}
