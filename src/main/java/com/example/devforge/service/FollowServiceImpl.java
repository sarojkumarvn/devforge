package com.example.devforge.service;

import java.time.LocalDateTime;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.example.devforge.dto.FollowRequestDto;
import com.example.devforge.dto.FollowResponseDto;
import com.example.devforge.entity.Follow;
import com.example.devforge.entity.User;
import com.example.devforge.exception.BadRequestException;
import com.example.devforge.exception.ConflictException;
import com.example.devforge.exception.ResourceNotFoundException;
import com.example.devforge.repository.FollowRepository;
import com.example.devforge.repository.UserRepository;
import com.example.devforge.security.AuthUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowServiceImpl implements FollowService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final AuthUtil authUtil;

    // ✅ FOLLOW USER
    @Override
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @CacheEvict(cacheNames = {"feedPages", "userProfiles", "userPages"}, allEntries = true)
    public FollowResponseDto followUser(FollowRequestDto dto) {
        Long followerId = authUtil.getCurrentUserId();

        if (followerId.equals(dto.getFollowingId())) {
            throw new BadRequestException("You can't follow yourself");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ResourceNotFoundException("Follower not found"));

        User following = userRepository.findById(dto.getFollowingId())
                .orElseThrow(() -> new ResourceNotFoundException("Following user not found"));

        boolean alreadyExists = followRepository
                .existsByFollowerIdAndFollowingId(follower.getId(), following.getId());

        if (alreadyExists) {
            throw new ConflictException("Already following");
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
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @CacheEvict(cacheNames = {"feedPages", "userProfiles", "userPages"}, allEntries = true)
    public String unfollowUser(FollowRequestDto dto) {
        Long followerId = authUtil.getCurrentUserId();

        boolean exists = followRepository
                .existsByFollowerIdAndFollowingId(
                        followerId,
                        dto.getFollowingId()
                );

        if (!exists) {
            throw new ResourceNotFoundException("Follow relationship does not exist");
        }

        followRepository.deleteByFollowerIdAndFollowingId(
                followerId,
                dto.getFollowingId()
        );

        return "Unfollowed successfully";
    }

    // ✅ GET FOLLOWERS (IDs only)
    @Override
    public Page<Long> getAllFollowers(Long userId, int page, int size) {
        return followRepository.findFollowerIds(userId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

   
    @Override
    public Page<Long> getAllFollowings(Long userId, int page, int size) {
        return followRepository.findFollowingIds(userId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }
}
