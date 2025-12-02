package com.gwtt.dagachi.dto;

import com.gwtt.dagachi.constants.PostingStatus;
import com.gwtt.dagachi.constants.PostingType;
import com.gwtt.dagachi.entity.Posting;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostingResponseDto {
  private Long id;
  private String title;
  private String description;
  private PostingType type;
  private PostingStatus status;
  private int maxCapacity;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Long authorId;
  private String authorNickname;

  public static PostingResponseDto of(Posting posting) {
    return PostingResponseDto.builder()
        .id(posting.getId())
        .title(posting.getTitle())
        .description(posting.getDescription())
        .type(posting.getType())
        .status(posting.getStatus())
        .maxCapacity(posting.getMaxCapacity())
        .createdAt(posting.getCreatedAt())
        .updatedAt(posting.getUpdatedAt())
        .authorId(posting.getAuthor().getId())
        .authorNickname(posting.getAuthor().getNickname())
        .build();
  }
}
