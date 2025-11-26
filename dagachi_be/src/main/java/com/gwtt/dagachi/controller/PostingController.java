package com.gwtt.dagachi.controller;

import com.gwtt.dagachi.Adapter.CustomUserDetails;
import com.gwtt.dagachi.dto.PostingCreateRequestDto;
import com.gwtt.dagachi.dto.PostingResponseDto;
import com.gwtt.dagachi.dto.PostingSimpleResponseDto;
import com.gwtt.dagachi.dto.PostingUpdateRequestDto;
import com.gwtt.dagachi.service.PostingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/postings")
@RequiredArgsConstructor
public class PostingController {
  private final PostingService postingService;

  @GetMapping
  public ResponseEntity<List<PostingSimpleResponseDto>> getPostings() {
    return ResponseEntity.ok(postingService.getAllPostings());
  }

  @GetMapping("/{id}")
  public ResponseEntity<PostingResponseDto> getPostingById(@PathVariable @NotNull Long id) {
    try {
      return ResponseEntity.ok(postingService.getPostingById(id));
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping
  public ResponseEntity<PostingResponseDto> createPosting(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody @Valid PostingCreateRequestDto postingRequestDto) {
    Long currentUserId = userDetails.getUserId();
    try {
      return ResponseEntity.ok(postingService.createPosting(currentUserId, postingRequestDto));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(null);
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<PostingResponseDto> updatePosting(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable @NotNull Long id,
      @RequestBody @Valid PostingUpdateRequestDto postingUpdateRequestDto) {
    Long currentUserId = userDetails.getUserId();
    try {
      PostingResponseDto updatedPosting =
          postingService.updatePosting(id, currentUserId, postingUpdateRequestDto);
      return ResponseEntity.ok(updatedPosting);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(null);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePosting(
      @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable @NotNull Long id) {
    try {
      Long currentUserId = userDetails.getUserId();
      postingService.deletePosting(id, currentUserId);
      return ResponseEntity.noContent().build();
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }
}
