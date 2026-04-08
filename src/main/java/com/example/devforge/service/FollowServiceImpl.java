package com.example.devforge.service;

import java.time.LocalDateTime;
import java.util.List;


import org.springframework.stereotype.Service;

import com.example.devforge.dto.FollowRequestDto;
import com.example.devforge.dto.FollowResponseDto;
import com.example.devforge.entity.Follow;
import com.example.devforge.entity.User;
import com.example.devforge.exception.ResourceNotFoundException;
import com.example.devforge.repository.FollowRepository;
import com.example.devforge.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowServiceImpl implements FollowService {
    private final UserRepository userRepository;
    private final FollowRepository followRepository;


    @Override
    public FollowResponseDto followUser(FollowRequestDto dto) {   // Implementation done
        if (dto.getFollowerId().equals(dto.getFollowingId())) {
            throw new RuntimeException("You cant follow yourself");

        }

        // Getting hte following id
        User following = userRepository.findById(dto.getFollowingId()).orElseThrow(
                () -> new ResourceNotFoundException("Following id is not found"));

        // Getting the follower Id
        User follower = userRepository.findById(dto.getFollowerId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Follower id is not found"));

        boolean alreadyExists = followRepository.existsByFollowerIdAndFollowingId(
                follower.getId(), following.getId());

        if (alreadyExists) {
            throw new RuntimeException("Already following");
        }

        Follow follow = new Follow();

        follow.setFollower(follower);
        follow.setFollowing(following);
        follow.setCreatedAt(LocalDateTime.now());

        followRepository.save(follow);

        return new FollowResponseDto(follower.getId(), following.getId(), "Followed successfully !");

    }

    @Override
    public String unfollowUser(FollowRequestDto dto) {    // Implementation done

        boolean exists = followRepository
                .existsByFollowerIdAndFollowingId(
                        dto.getFollowerId(),
                        dto.getFollowingId()
                );

        if (!exists) {
            throw new RuntimeException("Follow relationship does not exist");
        }

        followRepository.deleteByFollowerIdAndFollowingId(
                dto.getFollowerId(),
                dto.getFollowingId()
        );

        return "Unfollowed successfully";
      
    }

    @Override
    public List<Long> getAllFollowers(Long userId) {
        return followRepository.findByFollowingId(userId)
                        .stream()
                        .map( f -> f.getFollower().getId())
                        .toList();
       
    }

    @Override
    public List<Long> getAllFollowings(Long userId) {
          return followRepository.findByFollowerId(userId)
                        .stream()
                        .map( f -> f.getFollowing().getId())
                        .toList();
       
      
    }

}
