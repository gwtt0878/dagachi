package com.gwtt.dagachi.dto;

import com.gwtt.dagachi.constants.PostingType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostingRequestDto {

  @NotBlank(message = "제목은 필수 입력 항목입니다.")
  @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
  private String title;

  @NotBlank(message = "내용은 필수 입력 항목입니다.")
  private String description;

  @NotNull(message = "모집 정원은 필수 입력 항목입니다.")
  @Positive(message = "모집 정원은 양수여야 합니다.")
  private int maxCapacity;

  @NotNull(message = "게시글 타입은 필수 입력 항목입니다.")
  @Enumerated(EnumType.STRING)
  private PostingType type;
}
