package com.gwtt.dagachi.service;

import com.gwtt.dagachi.constants.Role;
import com.gwtt.dagachi.dto.SignupRequestDto;
import com.gwtt.dagachi.entity.User;
import com.gwtt.dagachi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

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
}
