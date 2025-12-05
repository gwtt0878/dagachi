package com.gwtt.dagachi.dto;

import com.gwtt.dagachi.constants.ParticipationStatus;
import com.gwtt.dagachi.entity.Participation;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParticipationSimpleResponseDto {
  private Long participationId;
  private LocalDateTime createdAt;
  private ParticipationStatus status;

  public static ParticipationSimpleResponseDto of(Participation participation) {
    return ParticipationSimpleResponseDto.builder()
        .participationId(participation.getId())
        .createdAt(participation.getCreatedAt())
        .status(participation.getStatus())
        .build();
  }
}
