package com.gwtt.dagachi.controller;

import com.gwtt.dagachi.Adapter.CustomUserDetails;
import com.gwtt.dagachi.dto.PostingSimpleResponseDto;
import com.gwtt.dagachi.dto.UserResponseDto;
import com.gwtt.dagachi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @GetMapping("/{id}")
  public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
    return ResponseEntity.ok(userService.getUserById(id));
  }

  @GetMapping("/me")
  public ResponseEntity<UserResponseDto> getCurrentUser(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(userService.getUserById(userDetails.getUserId()));
  }

  @GetMapping("/{id}/authored")
  public ResponseEntity<Page<PostingSimpleResponseDto>> getAuthoredPostingsByUserId(
      @PathVariable Long id,
      @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(userService.getAuthoredPostingsByUserId(id, pageable));
  }

  @GetMapping("/{id}/joined")
  public ResponseEntity<Page<PostingSimpleResponseDto>> getJoinedPostingsByUserId(
      @PathVariable Long id,
      @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(userService.getJoinedPostingsByUserId(id, pageable));
  }
}
