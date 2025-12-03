package com.gwtt.dagachi.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gwtt.dagachi.config.JwtAuthenticationEntryPoint;
import com.gwtt.dagachi.config.JwtAuthenticationFilter;
import com.gwtt.dagachi.config.JwtTokenProvider;
import com.gwtt.dagachi.config.TestSecurityConfig;
import com.gwtt.dagachi.constants.Role;
import com.gwtt.dagachi.dto.UserSimpleResponseDto;
import com.gwtt.dagachi.entity.User;
import com.gwtt.dagachi.exception.NotFoundUserException;
import com.gwtt.dagachi.service.AdminService;
import com.gwtt.dagachi.service.CustomUserDetailsService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("AdminController 테스트")
class AdminControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JwtAuthenticationFilter jwtAuthenticationFilter;

  @MockitoBean private AdminService adminService;
  @MockitoBean private JwtTokenProvider jwtTokenProvider;
  @MockitoBean private CustomUserDetailsService customUserDetailsService;
  @MockitoBean private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  private User adminUser;
  private User normalUser;

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
  }

  @Nested
  @DisplayName("GET /api/admin/users")
  class GetUsersTest {

    @Test
    @DisplayName("관리자가 사용자 목록을 조회한다")
    @WithMockUser(roles = "ADMIN")
    void success() throws Exception {
      // given
      Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
      List<UserSimpleResponseDto> users =
          List.of(UserSimpleResponseDto.of(adminUser), UserSimpleResponseDto.of(normalUser));
      Page<UserSimpleResponseDto> userPage = new PageImpl<>(users, pageable, users.size());

      given(adminService.getUsers(any(Pageable.class))).willReturn(userPage);

      // when & then
      mockMvc
          .perform(get("/api/admin/users").with(csrf()))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content").isArray())
          .andExpect(jsonPath("$.content.length()").value(2))
          .andExpect(jsonPath("$.content[0].username").value("admin"))
          .andExpect(jsonPath("$.content[0].nickname").value("관리자"))
          .andExpect(jsonPath("$.content[0].role").value("ADMIN"))
          .andExpect(jsonPath("$.content[1].username").value("user1"))
          .andExpect(jsonPath("$.content[1].role").value("USER"))
          .andExpect(jsonPath("$.totalElements").value(2))
          .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @DisplayName("페이지 파라미터로 두 번째 페이지를 조회한다")
    @WithMockUser(roles = "ADMIN")
    void withPageParameter() throws Exception {
      // given
      Pageable pageable = PageRequest.of(1, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
      List<UserSimpleResponseDto> users = List.of(UserSimpleResponseDto.of(normalUser));
      Page<UserSimpleResponseDto> userPage = new PageImpl<>(users, pageable, 6);

      given(adminService.getUsers(any(Pageable.class))).willReturn(userPage);

      // when & then
      mockMvc
          .perform(get("/api/admin/users").param("page", "1").with(csrf()))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content.length()").value(1))
          .andExpect(jsonPath("$.number").value(1))
          .andExpect(jsonPath("$.totalElements").value(6))
          .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    @DisplayName("일반 사용자는 접근할 수 없다")
    @WithMockUser(roles = "USER")
    void forbiddenForNormalUser() throws Exception {
      // when & then
      mockMvc
          .perform(get("/api/admin/users").with(csrf()))
          .andDo(print())
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("PUT /api/admin/users/{id}/role")
  class UpdateUserRoleTest {

    @Test
    @DisplayName("관리자가 사용자 역할을 변경한다")
    @WithMockUser(roles = "ADMIN")
    void success() throws Exception {
      // given
      willDoNothing().given(adminService).updateUserRole(eq(2L), eq(Role.ADMIN));

      // when & then
      mockMvc
          .perform(
              put("/api/admin/users/2/role")
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("\"ADMIN\""))
          .andDo(print())
          .andExpect(status().isNoContent());

      then(adminService).should().updateUserRole(2L, Role.ADMIN);
    }

    @Test
    @DisplayName("관리자를 일반 사용자로 강등한다")
    @WithMockUser(roles = "ADMIN")
    void demoteToUser() throws Exception {
      // given
      willDoNothing().given(adminService).updateUserRole(eq(1L), eq(Role.USER));

      // when & then
      mockMvc
          .perform(
              put("/api/admin/users/1/role")
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("\"USER\""))
          .andDo(print())
          .andExpect(status().isNoContent());

      then(adminService).should().updateUserRole(1L, Role.USER);
    }

    @Test
    @DisplayName("일반 사용자는 역할을 변경할 수 없다")
    @WithMockUser(roles = "USER")
    void forbiddenForNormalUser() throws Exception {
      // when & then
      mockMvc
          .perform(
              put("/api/admin/users/2/role")
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("\"ADMIN\""))
          .andDo(print())
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 역할 변경 시 예외가 발생한다")
    @WithMockUser(roles = "ADMIN")
    void userNotFound() throws Exception {
      // given
      willThrow(new NotFoundUserException("사용자를 찾을 수 없습니다."))
          .given(adminService)
          .updateUserRole(eq(999L), any(Role.class));

      // when & then
      mockMvc
          .perform(
              put("/api/admin/users/999/role")
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("\"ADMIN\""))
          .andDo(print())
          .andExpect(status().isBadRequest());
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
