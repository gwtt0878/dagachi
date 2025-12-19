package com.gwtt.dagachi.dto;

import com.gwtt.dagachi.entity.Comment;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentResponseDto {
  private Long id;
  private Long parentCommentId;
  private Long authorId;
  private String authorNickname;
  private String content;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;
  private Integer depth;

  public static CommentResponseDto of(Comment comment) {
    return CommentResponseDto.builder()
        .id(comment.getId())
        .parentCommentId(
            comment.getParentComment() != null ? comment.getParentComment().getId() : null)
        .authorId(comment.getAuthor().getId())
        .content(comment.getDeletedAt() != null ? "[삭제된 댓글입니다.]" : comment.getContent())
        .createdAt(comment.getCreatedAt())
        .updatedAt(comment.getUpdatedAt())
        .deletedAt(comment.getDeletedAt())
        .authorNickname(comment.getAuthor().getNickname())
        .depth(comment.getDepth())
        .build();
  }
}
