package com.gwtt.dagachi.service;

import com.gwtt.dagachi.config.JwtTokenProvider;
import com.gwtt.dagachi.constants.Role;
import com.gwtt.dagachi.dto.LoginRequestDto;
import com.gwtt.dagachi.dto.SignupRequestDto;
import com.gwtt.dagachi.entity.User;
import com.gwtt.dagachi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;

  public void signup(SignupRequestDto signupRequestDto) {
    if (userRepository.existsByUsername(signupRequestDto.getUsername())) {
      throw new RuntimeException("이미 존재하는 사용자 ID입니다.");
    }

    User user =
        User.builder()
            .username(signupRequestDto.getUsername())
            .password(passwordEncoder.encode(signupRequestDto.getPassword()))
            .nickname(signupRequestDto.getNickname())
            .role(Role.USER)
            .build();

    userRepository.save(user);
  }

  public String login(LoginRequestDto loginRequestDto) {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(
            loginRequestDto.getUsername(), loginRequestDto.getPassword());
    try {
      Authentication authentication = authenticationManager.authenticate(authenticationToken);
      SecurityContextHolder.getContext().setAuthentication(authentication);
      String jwt = jwtTokenProvider.generateToken(authentication);
      return jwt;
    } catch (Exception e) {
      throw new RuntimeException("로그인에 실패했습니다. : " + e.getMessage());
    }
  }
}
