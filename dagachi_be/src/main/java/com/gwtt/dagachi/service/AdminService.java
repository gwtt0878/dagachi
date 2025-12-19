package com.gwtt.dagachi.service;

import com.gwtt.dagachi.constants.Role;
import com.gwtt.dagachi.dto.UserSimpleResponseDto;
import com.gwtt.dagachi.entity.User;
import com.gwtt.dagachi.exception.DagachiException;
import com.gwtt.dagachi.exception.ErrorCode;
import com.gwtt.dagachi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AdminService {
  private final UserRepository userRepository;

  @Transactional
  public void updateUserRole(Long userId, Role role) {
    User user =
        userRepository
            .findByIdForUpdate(userId)
            .orElseThrow(() -> new DagachiException(ErrorCode.USER_NOT_FOUND));
    user.updateRole(role);
  }

  @Transactional(readOnly = true)
  public Page<UserSimpleResponseDto> getUsers(Pageable pageable) {
    Page<User> users = userRepository.findAll(pageable);
    return users.map(UserSimpleResponseDto::of);
  }
}
