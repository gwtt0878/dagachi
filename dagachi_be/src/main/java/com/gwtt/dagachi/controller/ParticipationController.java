package com.gwtt.dagachi.controller;

import com.gwtt.dagachi.Adapter.CustomUserDetails;
import com.gwtt.dagachi.service.ParticipationService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/participation")
@RequiredArgsConstructor
public class ParticipationController {
  private final ParticipationService participationService;

  @PostMapping("/{postingId}")
  public ResponseEntity<Void> joinPosting(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable @NotNull Long postingId) {
    try {
      Long currentUserId = userDetails.getUserId();
      participationService.joinPosting(currentUserId, postingId);
      return ResponseEntity.noContent().build();
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(null);
    }
  }
}
