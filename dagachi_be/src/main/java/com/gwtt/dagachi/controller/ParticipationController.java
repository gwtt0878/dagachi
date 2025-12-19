package com.gwtt.dagachi.controller;

import com.gwtt.dagachi.adapter.CustomUserDetails;
import com.gwtt.dagachi.dto.ParticipationResponseDto;
import com.gwtt.dagachi.dto.ParticipationSimpleResponseDto;
import com.gwtt.dagachi.service.ParticipationService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/participation")
@RequiredArgsConstructor
public class ParticipationController {
  private final ParticipationService participationService;

  @GetMapping("/{postingId}/check")
  public ResponseEntity<ParticipationSimpleResponseDto> checkParticipation(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable @NotNull Long postingId) {
    Long currentUserId = userDetails.getUserId();
    ParticipationSimpleResponseDto participation =
        participationService.getSimpleParticipation(currentUserId, postingId);
    return ResponseEntity.ok(participation);
  }

  @GetMapping("/{postingId}/me")
  public ResponseEntity<ParticipationResponseDto> getMyParticipation(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable @NotNull Long postingId) {
    Long currentUserId = userDetails.getUserId();
    ParticipationResponseDto participation =
        participationService.getMyParticipation(currentUserId, postingId);
    return ResponseEntity.ok(participation);
  }

  @PostMapping("/{postingId}")
  public ResponseEntity<Void> joinPosting(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable @NotNull Long postingId) {
    Long currentUserId = userDetails.getUserId();
    participationService.joinPosting(currentUserId, postingId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{postingId}")
  public ResponseEntity<Void> leavePosting(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable @NotNull Long postingId) {
    Long currentUserId = userDetails.getUserId();
    participationService.leavePosting(currentUserId, postingId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{postingId}/approve/{participationId}")
  public ResponseEntity<Void> approveUser(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable @NotNull Long postingId,
      @PathVariable @NotNull Long participationId) {
    Long currentUserId = userDetails.getUserId();
    participationService.approveUser(currentUserId, participationId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{postingId}/user/{participationId}")
  public ResponseEntity<Void> rejectUser(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable @NotNull Long postingId,
      @PathVariable @NotNull Long participationId) {
    Long currentUserId = userDetails.getUserId();
    participationService.rejectUser(currentUserId, participationId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{postingId}")
  public ResponseEntity<PagedModel<ParticipationResponseDto>> getParticipations(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable @NotNull Long postingId,
      @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    Long currentUserId = userDetails.getUserId();
    Page<ParticipationResponseDto> participations =
        participationService.getParticipationsByPostingId(currentUserId, postingId, pageable);
    return ResponseEntity.ok(new PagedModel<>(participations));
  }
}
