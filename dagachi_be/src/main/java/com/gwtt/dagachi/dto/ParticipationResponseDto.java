package com.gwtt.dagachi.dto;

import com.gwtt.dagachi.constants.ParticipationStatus;
import com.gwtt.dagachi.entity.Participation;
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
public class ParticipationResponseDto {
  private Long participationId;
  private Long postingId;
  private Long participantId;
  private String participantNickname;
  private ParticipationStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static ParticipationResponseDto of(Participation participation) {
    return ParticipationResponseDto.builder()
        .participationId(participation.getId())
        .postingId(participation.getPosting().getId())
        .participantId(participation.getParticipant().getId())
        .participantNickname(participation.getParticipant().getNickname())
        .status(participation.getStatus())
        .createdAt(participation.getCreatedAt())
        .updatedAt(participation.getUpdatedAt())
        .build();
  }
}
