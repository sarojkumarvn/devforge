package com.example.devforge.service;

import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.example.devforge.dto.CommentRequestDto;
import com.example.devforge.dto.CommentResponseDto;
import com.example.devforge.dto.ReplyRequestDto;
import com.example.devforge.entity.Comment;
import com.example.devforge.entity.Project;
import com.example.devforge.entity.User;
import com.example.devforge.exception.ResourceNotFoundException;
import com.example.devforge.repository.CommentRepository;
import com.example.devforge.repository.ProjectRepository;
import com.example.devforge.repository.UserRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class CommentServiceImple implements CommentService {
    private final UserRepository userRepository ;
    private final ProjectRepository projectRepository ;
    private final CommentRepository commentRepository; 
    private final ModelMapper modelMapper ;

    @Override
    public void addComment(CommentRequestDto dto) {
        User user = userRepository.findById(dto.getUserId()).orElseThrow(()-> new ResourceNotFoundException("User not found with this id : {}" + dto.getUserId()));


        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Comment comment = new Comment();
        comment.setContent(dto.getContent());
        comment.setUser(user);
        comment.setProject(project);
        comment.setCreatedAt(LocalDateTime.now());

        commentRepository.save(comment) ;

        
    }

    @Override
    public void replyToComment(Long commentId, ReplyRequestDto dto) {
    
    Comment parent = commentRepository.findById(commentId).
    orElseThrow(()->
     new ResourceNotFoundException
     ("Comment not found with this id : {}" + commentId));

    User user = userRepository.findById(dto.getUserId())
    .orElseThrow(()->
     new ResourceNotFoundException
     ("User not found with this id : {}" 
     + dto.getUserId()));






       
    }

    @Override
    public List<CommentResponseDto> getCommentsByProject(Long projectId) {
        List<Comment> comments = commentRepository.findByProjectIdAndParentIsNull(projectId);


        return comments.stream()
        .map(comment ->
             modelMapper.map
             (comment , CommentResponseDto.class))
             .toList();


      
    }

}
