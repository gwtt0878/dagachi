package com.gwtt.dagachi.controller;

import com.gwtt.dagachi.adapter.CustomUserDetails;
import com.gwtt.dagachi.dto.CommentCreateRequestDto;
import com.gwtt.dagachi.dto.CommentResponseDto;
import com.gwtt.dagachi.dto.CommentUpdateRequestDto;
import com.gwtt.dagachi.service.CommentService;
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
@RequestMapping("/api/postings/{postingId}/comment")
@RequiredArgsConstructor
public class CommentController {
  private final CommentService commentService;

  @GetMapping
  public ResponseEntity<List<CommentResponseDto>> getComments(@PathVariable Long postingId) {
    return ResponseEntity.ok(commentService.getComments(postingId));
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
      @PathVariable Long commentId,
      @RequestBody CommentUpdateRequestDto commentUpdateRequestDto) {
    Long userId = userDetails.getUserId();
    return ResponseEntity.ok(
        commentService.updateComment(commentId, userId, commentUpdateRequestDto));
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long commentId) {
    Long userId = userDetails.getUserId();
    commentService.deleteComment(commentId, userId);
    return ResponseEntity.noContent().build();
  }
}
