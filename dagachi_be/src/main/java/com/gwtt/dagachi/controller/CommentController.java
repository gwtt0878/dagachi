package com.gwtt.dagachi.controller;

import com.gwtt.dagachi.adapter.CustomUserDetails;
import com.gwtt.dagachi.dto.CommentCreateRequestDto;
import com.gwtt.dagachi.dto.CommentResponseDto;
import com.gwtt.dagachi.dto.CommentUpdateRequestDto;
import com.gwtt.dagachi.service.CommentService;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/postings/{postingId}/comment")
@RequiredArgsConstructor
public class CommentController {
  private final CommentService commentService;

  @GetMapping
  public ResponseEntity<PagedModel<CommentResponseDto>> getComments(
      @PathVariable Long postingId,
      @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(new PagedModel<>(commentService.getComments(postingId, pageable)));
  }

  @PostMapping
  public ResponseEntity<CommentResponseDto> createComment(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long postingId,
      @RequestBody CommentCreateRequestDto commentCreateRequestDto) {
    Long userId = userDetails.getUserId();
    return ResponseEntity.ok(
        commentService.createComment(postingId, userId, commentCreateRequestDto));
  }

  @PutMapping("/{commentId}")
  public ResponseEntity<CommentResponseDto> updateComment(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long postingId,
      @PathVariable Long commentId,
      @RequestBody CommentUpdateRequestDto commentUpdateRequestDto) {
    Long currentUserId = userDetails.getUserId();
    return ResponseEntity.ok(
        commentService.updateComment(postingId, commentId, currentUserId, commentUpdateRequestDto));
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long postingId, @PathVariable Long commentId) {
    Long currentUserId = userDetails.getUserId();
    commentService.deleteComment(postingId, commentId, currentUserId);
    return ResponseEntity.noContent().build();
  }
}
