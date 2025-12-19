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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {
  private final CommentRepository commentRepository;
  private final PostingRepository postingRepository;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public Page<CommentResponseDto> getComments(Long postingId, Pageable pageable) {
    Posting posting =
        postingRepository
            .findById(postingId)
            .orElseThrow(() -> new DagachiException(ErrorCode.POSTING_NOT_FOUND));
    Page<Comment> comments = commentRepository.findByPostingFetched(posting, pageable);
    return comments.map(CommentResponseDto::of);
  }

  @Transactional(readOnly = true)
  public CommentResponseDto getCommentById(Long commentId) {
    Comment comment =
        commentRepository
            .findByIdFetched(commentId)
            .orElseThrow(() -> new DagachiException(ErrorCode.COMMENT_NOT_FOUND));
    return CommentResponseDto.of(comment);
  }

  @Transactional
  public CommentResponseDto createComment(
      Long postingId, Long userId, CommentCreateRequestDto commentCreateRequestDto) {
    Posting posting =
        postingRepository
            .findById(postingId)
            .orElseThrow(() -> new DagachiException(ErrorCode.POSTING_NOT_FOUND));
    User author =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new DagachiException(ErrorCode.USER_NOT_FOUND));

    Integer depth = 0;
    Comment parentComment = null;

    if (commentCreateRequestDto.getParentCommentId() != null) {
      parentComment =
          commentRepository
              .findByIdForUpdate(commentCreateRequestDto.getParentCommentId())
              .orElseThrow(() -> new DagachiException(ErrorCode.COMMENT_NOT_FOUND));
      if (parentComment.getDeletedAt() != null) {
        throw new DagachiException(ErrorCode.COMMENT_ALREADY_DELETED);
      }
      if (!parentComment.getPosting().getId().equals(postingId)) {
        throw new DagachiException(ErrorCode.COMMENT_POSTING_NOT_MATCHED);
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
    Comment fetchedComment =
        commentRepository
            .findByIdFetched(savedComment.getId())
            .orElseThrow(() -> new DagachiException(ErrorCode.INTERNAL_SERVER_ERROR));
    return CommentResponseDto.of(fetchedComment);
  }

  @Transactional
  public CommentResponseDto updateComment(
      Long postingId,
      Long commentId,
      Long currentUserId,
      CommentUpdateRequestDto commentUpdateRequestDto) {
    Comment comment =
        commentRepository
            .findByIdForUpdate(commentId)
            .orElseThrow(() -> new DagachiException(ErrorCode.COMMENT_NOT_FOUND));

    if (!comment.getAuthor().getId().equals(currentUserId)) {
      throw new DagachiException(ErrorCode.COMMENT_NOT_AUTHORIZED);
    }
    if (comment.getDeletedAt() != null) {
      throw new DagachiException(ErrorCode.COMMENT_ALREADY_DELETED);
    }
    if (!comment.getPosting().getId().equals(postingId)) {
      throw new DagachiException(ErrorCode.COMMENT_POSTING_NOT_MATCHED);
    }
    comment.updateContent(commentUpdateRequestDto.getContent());
    return CommentResponseDto.of(comment);
  }

  @Transactional
  public void deleteComment(Long postingId, Long commentId, Long currentUserId) {
    Comment comment =
        commentRepository
            .findByIdForUpdate(commentId)
            .orElseThrow(() -> new DagachiException(ErrorCode.COMMENT_NOT_FOUND));
    if (!comment.getAuthor().getId().equals(currentUserId)) {
      throw new DagachiException(ErrorCode.COMMENT_NOT_AUTHORIZED);
    }
    if (!comment.getPosting().getId().equals(postingId)) {
      throw new DagachiException(ErrorCode.COMMENT_POSTING_NOT_MATCHED);
    }
    comment.delete();
  }
}
