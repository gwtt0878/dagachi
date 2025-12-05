package com.gwtt.dagachi.service;

import com.gwtt.dagachi.adapter.CustomUserDetails;
import com.gwtt.dagachi.entity.User;
import com.gwtt.dagachi.exception.DagachiException;
import com.gwtt.dagachi.exception.ErrorCode;
import com.gwtt.dagachi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new DagachiException(ErrorCode.USER_NOT_FOUND));
    return new CustomUserDetails(user);
  }
}
