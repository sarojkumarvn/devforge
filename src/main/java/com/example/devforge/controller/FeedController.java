package com.example.devforge.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.devforge.dto.FeedResponseDto;
import com.example.devforge.service.FeedService;

import lombok.RequiredArgsConstructor;


//TESTED
@RestController
@RequestMapping("/feed")
@RequiredArgsConstructor
public class FeedController {
    private final FeedService feedService ;

    @GetMapping
    public ResponseEntity<Page<FeedResponseDto>> getFeeds (
        @RequestParam(defaultValue = "0") int page ,
        @RequestParam(defaultValue = "10") int size ,
        @RequestParam Long userId
    ) {
        return ResponseEntity.ok(
            feedService.getFeed(page, size, userId)
        ) ;
    }




    @GetMapping("/following") 
    public ResponseEntity<Page<FeedResponseDto>> getFollowingFeeds (
        @RequestParam Long userId ,
        @RequestParam(defaultValue = "0") int page ,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
            feedService.getFollowingFeed(page, size, userId)
        ) ;
    }




}
