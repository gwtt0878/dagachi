package com.gwtt.dagachi.controller;

import com.gwtt.dagachi.constants.Role;
import com.gwtt.dagachi.dto.UserSimpleResponseDto;
import com.gwtt.dagachi.service.AdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
  private final AdminService adminService;

  @GetMapping("/users")
  public ResponseEntity<Page<UserSimpleResponseDto>> getUsers(
      @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(adminService.getUsers(pageable));
  }

  @PutMapping("/users/{id}/role")
  public ResponseEntity<Void> updateUserRole(
      @PathVariable @NotNull Long id, @RequestBody @Valid @NotNull Role role) {
    adminService.updateUserRole(id, role);
    return ResponseEntity.noContent().build();
  }
}
