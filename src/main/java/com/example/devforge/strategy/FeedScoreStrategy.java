package com.example.devforge.strategy;

import com.example.devforge.entity.Project;

public interface FeedScoreStrategy {
    double calculateScore(Project project) ;
    

}
