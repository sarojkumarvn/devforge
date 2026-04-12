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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowServiceImpl implements FollowService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    // ✅ FOLLOW USER
    @Override
    public FollowResponseDto followUser(FollowRequestDto dto) {

        if (dto.getFollowerId().equals(dto.getFollowingId())) {
            throw new RuntimeException("You can't follow yourself");
        }

        User follower = userRepository.findById(dto.getFollowerId())
                .orElseThrow(() -> new ResourceNotFoundException("Follower not found"));

        User following = userRepository.findById(dto.getFollowingId())
                .orElseThrow(() -> new ResourceNotFoundException("Following user not found"));

        boolean alreadyExists = followRepository
                .existsByFollowerIdAndFollowingId(follower.getId(), following.getId());

        if (alreadyExists) {
            throw new RuntimeException("Already following");
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        follow.setCreatedAt(LocalDateTime.now());

        followRepository.save(follow);

        return new FollowResponseDto(
                follower.getId(),
                following.getId(),
                "Followed successfully"
        );
    }

    // ✅ UNFOLLOW USER
    @Override
    public String unfollowUser(FollowRequestDto dto) {

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

    // ✅ GET FOLLOWERS (IDs only)
    @Override
    public List<Long> getAllFollowers(Long userId) {
        return followRepository.findFollowerIds(userId);
    }

   
    @Override
    public List<Long> getAllFollowings(Long userId) {
        return followRepository.findFollowingIds(userId);
    }
}