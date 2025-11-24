package com.gwtt.dagachi.controller;

import com.gwtt.dagachi.dto.SignupRequestDto;
import com.gwtt.dagachi.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @PostMapping("/signup")
  public ResponseEntity<String> signup(@RequestBody SignupRequestDto signupRequestDto) {
    try {
      authService.signup(signupRequestDto);
      return ResponseEntity.ok("회원가입이 완료되었습니다.");
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body("회원가입에 실패했습니다. " + e.getMessage());
    }
  }
}
