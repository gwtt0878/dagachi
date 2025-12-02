package com.gwtt.dagachi.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.gwtt.dagachi.constants.Role;
import com.gwtt.dagachi.dto.UserSimpleResponseDto;
import com.gwtt.dagachi.entity.User;
import com.gwtt.dagachi.exception.NotFoundUserException;
import com.gwtt.dagachi.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.context.annotation.Import;
import com.gwtt.dagachi.config.TestQueryDSLConfig;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@Import({TestQueryDSLConfig.class})
@ActiveProfiles("test")
@DisplayName("AdminService 단위 테스트")
class AdminServiceTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private AdminService adminService;

  private User adminUser;
  private User normalUser;
  private User anotherUser;

  @BeforeEach
  void setUp() {
    adminUser =
        User.builder()
            .username("admin")
            .password("password")
            .role(Role.ADMIN)
            .nickname("관리자")
            .build();
    setId(adminUser, 1L);

    normalUser =
        User.builder()
            .username("user1")
            .password("password")
            .role(Role.USER)
            .nickname("일반사용자")
            .build();
    setId(normalUser, 2L);

    anotherUser =
        User.builder()
            .username("user2")
            .password("password")
            .role(Role.USER)
            .nickname("다른사용자")
            .build();
    setId(anotherUser, 3L);
  }

  @Nested
  @DisplayName("getUsers 메서드")
  class GetUsersTest {

    @Test
    @DisplayName("페이징된 사용자 목록을 조회한다")
    void success() {
      // given
      Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
      List<User> users = List.of(adminUser, normalUser, anotherUser);
      Page<User> userPage = new PageImpl<>(users, pageable, users.size());

      given(userRepository.findAll(pageable)).willReturn(userPage);

      // when
      Page<UserSimpleResponseDto> result = adminService.getUsers(pageable);

      // then
      assertThat(result.getContent()).hasSize(3);
      assertThat(result.getContent().get(0).getUsername()).isEqualTo("admin");
      assertThat(result.getContent().get(0).getNickname()).isEqualTo("관리자");
      assertThat(result.getContent().get(0).getRole()).isEqualTo(Role.ADMIN);
      assertThat(result.getContent().get(1).getUsername()).isEqualTo("user1");
      assertThat(result.getContent().get(1).getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("사용자가 없으면 빈 페이지를 반환한다")
    void emptyUsers() {
      // given
      Pageable pageable = PageRequest.of(0, 5);
      Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

      given(userRepository.findAll(pageable)).willReturn(emptyPage);

      // when
      Page<UserSimpleResponseDto> result = adminService.getUsers(pageable);

      // then
      assertThat(result.getContent()).isEmpty();
      assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("두 번째 페이지를 조회한다")
    void secondPage() {
      // given
      Pageable pageable = PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "createdAt"));
      List<User> users = List.of(anotherUser);
      Page<User> userPage = new PageImpl<>(users, pageable, 3); // 총 3개 중 1개 (2페이지)

      given(userRepository.findAll(pageable)).willReturn(userPage);

      // when
      Page<UserSimpleResponseDto> result = adminService.getUsers(pageable);

      // then
      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getNumber()).isEqualTo(1); // 현재 페이지 인덱스
      assertThat(result.getTotalElements()).isEqualTo(3);
      assertThat(result.getTotalPages()).isEqualTo(2);
    }
  }

  @Nested
  @DisplayName("updateUserRole 메서드")
  class UpdateUserRoleTest {

    @Test
    @DisplayName("일반 사용자를 관리자로 변경한다")
    void promoteToAdmin() {
      // given
      given(userRepository.findById(2L)).willReturn(Optional.of(normalUser));

      // when
      adminService.updateUserRole(2L, Role.ADMIN);

      // then
      assertThat(normalUser.getRole()).isEqualTo(Role.ADMIN);
      then(userRepository).should().save(normalUser);
    }

    @Test
    @DisplayName("관리자를 일반 사용자로 변경한다")
    void demoteToUser() {
      // given
      given(userRepository.findById(1L)).willReturn(Optional.of(adminUser));

      // when
      adminService.updateUserRole(1L, Role.USER);

      // then
      assertThat(adminUser.getRole()).isEqualTo(Role.USER);
      then(userRepository).should().save(adminUser);
    }

    @Test
    @DisplayName("같은 역할로 변경해도 정상 처리된다")
    void sameRole() {
      // given
      given(userRepository.findById(2L)).willReturn(Optional.of(normalUser));

      // when
      adminService.updateUserRole(2L, Role.USER);

      // then
      assertThat(normalUser.getRole()).isEqualTo(Role.USER);
      then(userRepository).should().save(normalUser);
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 역할 변경 시 예외가 발생한다")
    void userNotFound() {
      // given
      given(userRepository.findById(999L)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> adminService.updateUserRole(999L, Role.ADMIN))
          .isInstanceOf(NotFoundUserException.class)
          .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }
  }

  // Reflection으로 ID 설정하는 헬퍼 메서드
  private void setId(Object entity, Long id) {
    try {
      var idField = entity.getClass().getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(entity, id);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}

