package com.gwtt.dagachi.dto;

import com.gwtt.dagachi.constants.Role;
import com.gwtt.dagachi.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSimpleResponseDto {
  private Long id;
  private String username;
  private String nickname;
  private Role role;

  public static UserSimpleResponseDto of(User user) {
    return UserSimpleResponseDto.builder()
        .id(user.getId())
        .username(user.getUsername())
        .nickname(user.getNickname())
        .role(user.getRole())
        .build();
  }
}
