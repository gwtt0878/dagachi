package com.gwtt.dagachi.controller;

import com.gwtt.dagachi.dto.LoginRequestDto;
import com.gwtt.dagachi.dto.SignupRequestDto;
import com.gwtt.dagachi.service.AuthService;
import jakarta.validation.Valid;
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
  public ResponseEntity<String> signup(@Valid @RequestBody SignupRequestDto signupRequestDto) {
    try {
      authService.signup(signupRequestDto);
      return ResponseEntity.ok("회원가입이 완료되었습니다.");
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body("회원가입에 실패했습니다. " + e.getMessage());
    }
  }

  @PostMapping("/login")
  public ResponseEntity<String> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
    try {
      String jwt = authService.login(loginRequestDto);
      return ResponseEntity.ok(jwt);
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인에 실패했습니다. " + e.getMessage());
    }
  }
}
