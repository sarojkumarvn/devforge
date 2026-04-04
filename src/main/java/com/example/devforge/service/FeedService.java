package com.example.devforge.service;



import org.springframework.data.domain.Page;

import com.example.devforge.dto.FeedResponseDto;

public interface FeedService {
    Page<FeedResponseDto> getFeed(int page , int size , Long userId) ;

}
