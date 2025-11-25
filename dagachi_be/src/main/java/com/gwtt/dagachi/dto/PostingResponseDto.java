package com.gwtt.dagachi.dto;

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
  private String type; // PROJECT, STUDY
  private LocalDateTime createdAt;

  public static PostingResponseDto of(Posting posting) {
    return PostingResponseDto.builder()
        .id(posting.getId())
        .title(posting.getTitle())
        .description(posting.getDescription())
        .type(posting.getType())
        .createdAt(posting.getCreatedAt())
        .build();
  }
}
