package com.example.devforge.strategy;

import java.time.Duration;
import java.time.LocalDateTime;

import com.example.devforge.entity.Project;

public class DefaultFeedScoreStrategy implements FeedScoreStrategy{

    @Override
    public double calculateScore(Project project) {
        Long hours = Duration.between(project.getCreatedAt(), LocalDateTime.now()).toHours();

           double engagement =
                (project.getLikeCount() * 2) +
                (project.getCommentCount() * 3) +
                (project.getBookmarkCount() * 4);

        double decay = 1.0 / (hours + 1);

        return engagement * decay;
         
    }

}
