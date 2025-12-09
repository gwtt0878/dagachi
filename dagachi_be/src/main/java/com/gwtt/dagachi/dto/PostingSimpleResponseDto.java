package com.gwtt.dagachi.dto;

import com.gwtt.dagachi.constants.PostingStatus;
import com.gwtt.dagachi.constants.PostingType;
import com.gwtt.dagachi.entity.Posting;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostingSimpleResponseDto {
  private Long id;
  private String title;
  private PostingType type;
  private PostingStatus status;
  private int maxCapacity;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Double latitude;
  private Double longitude;
  private Long authorId;
  private String authorNickname;

  public static PostingSimpleResponseDto of(Posting posting) {
    return PostingSimpleResponseDto.builder()
        .id(posting.getId())
        .title(posting.getTitle())
        .type(posting.getType())
        .status(posting.getStatus())
        .maxCapacity(posting.getMaxCapacity())
        .createdAt(posting.getCreatedAt())
        .updatedAt(posting.getUpdatedAt())
        .latitude(posting.getLocation().getLatitude())
        .longitude(posting.getLocation().getLongitude())
        .authorId(posting.getAuthor().getId())
        .authorNickname(posting.getAuthor().getNickname())
        .build();
  }
}
