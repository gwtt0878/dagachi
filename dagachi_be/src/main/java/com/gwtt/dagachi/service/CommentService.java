package com.gwtt.dagachi.service;

import com.gwtt.dagachi.dto.CommentCreateRequestDto;
import com.gwtt.dagachi.dto.CommentResponseDto;
import com.gwtt.dagachi.dto.CommentUpdateRequestDto;
import com.gwtt.dagachi.entity.Comment;
import com.gwtt.dagachi.entity.Posting;
import com.gwtt.dagachi.entity.User;
import com.gwtt.dagachi.exception.DagachiException;
import com.gwtt.dagachi.exception.ErrorCode;
import com.gwtt.dagachi.repository.CommentRepository;
import com.gwtt.dagachi.repository.PostingRepository;
import com.gwtt.dagachi.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {
  private final CommentRepository commentRepository;
  private final PostingRepository postingRepository;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public List<CommentResponseDto> getComments(Long postingId) {
    Posting posting =
        postingRepository
            .findById(postingId)
            .orElseThrow(() -> new DagachiException(ErrorCode.COMMENT_NOT_FOUND));
    List<Comment> comments = commentRepository.findByPosting(posting);
    return comments.stream().map(CommentResponseDto::of).toList();
  }

  @Transactional(readOnly = true)
  public CommentResponseDto getCommentById(Long commentId) {
    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new DagachiException(ErrorCode.COMMENT_NOT_FOUND));
    return CommentResponseDto.of(comment);
  }

  @Transactional
  public CommentResponseDto createComment(
      Long postingId, Long userId, CommentCreateRequestDto commentCreateRequestDto) {
    Posting posting =
        postingRepository
            .findById(postingId)
            .orElseThrow(() -> new DagachiException(ErrorCode.COMMENT_NOT_FOUND));
    User author =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new DagachiException(ErrorCode.USER_NOT_FOUND));

    Integer depth = 0;
    Comment parentComment = null;

    if (commentCreateRequestDto.getParentCommentId() != null) {
      parentComment =
          commentRepository
              .findById(commentCreateRequestDto.getParentCommentId())
              .orElseThrow(() -> new DagachiException(ErrorCode.COMMENT_NOT_FOUND));
      if (!parentComment.getPosting().getId().equals(postingId)) {
        throw new DagachiException(ErrorCode.COMMENT_NOT_FOUND);
      }
      depth = parentComment.getDepth() + 1;
    }

    Comment comment =
        Comment.builder()
            .posting(posting)
            .author(author)
            .content(commentCreateRequestDto.getContent())
            .depth(depth)
            .parentComment(parentComment)
            .build();

    Comment savedComment = commentRepository.save(comment);
    return CommentResponseDto.of(savedComment);
  }

  @Transactional
  public CommentResponseDto updateComment(
      Long commentId, Long userId, CommentUpdateRequestDto commentUpdateRequestDto) {
    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new DagachiException(ErrorCode.COMMENT_NOT_FOUND));
    if (!comment.getAuthor().getId().equals(userId)) {
      throw new DagachiException(ErrorCode.COMMENT_NOT_AUTHORIZED);
    }

    comment.updateContent(commentUpdateRequestDto.getContent());
    Comment savedComment = commentRepository.save(comment);
    return CommentResponseDto.of(savedComment);
  }

  @Transactional
  public void deleteComment(Long commentId, Long userId) {
    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new DagachiException(ErrorCode.COMMENT_NOT_FOUND));
    if (!comment.getAuthor().getId().equals(userId)) {
      throw new DagachiException(ErrorCode.COMMENT_NOT_AUTHORIZED);
    }
    comment.delete();
    commentRepository.save(comment);
  }
}
