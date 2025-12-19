package com.gwtt.dagachi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentCreateRequestDto {
  private Long parentCommentId;

  @NotBlank(message = "댓글 내용은 필수 입력 항목입니다.")
  @Size(max = 500, message = "댓글 내용은 500자 이하여야 합니다.")
  private String content;
}
