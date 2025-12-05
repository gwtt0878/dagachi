package com.gwtt.dagachi.service;

import com.gwtt.dagachi.config.JwtTokenProvider;
import com.gwtt.dagachi.constants.Role;
import com.gwtt.dagachi.dto.LoginRequestDto;
import com.gwtt.dagachi.dto.SignupRequestDto;
import com.gwtt.dagachi.entity.User;
import com.gwtt.dagachi.exception.DagachiException;
import com.gwtt.dagachi.exception.ErrorCode;
import com.gwtt.dagachi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;

  @Transactional
  public void signup(SignupRequestDto signupRequestDto) {
    if (userRepository.existsByUsername(signupRequestDto.getUsername())) {
      throw new DagachiException(ErrorCode.DUPLICATE_USERNAME);
    }

    if (userRepository.existsByNickname(signupRequestDto.getNickname())) {
      throw new DagachiException(ErrorCode.DUPLICATE_NICKNAME);
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
      throw new DagachiException(ErrorCode.LOGIN_FAILED);
    }
  }
}
