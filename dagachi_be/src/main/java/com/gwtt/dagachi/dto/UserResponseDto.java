package com.gwtt.dagachi.dto;

import com.gwtt.dagachi.constants.Role;
import com.gwtt.dagachi.entity.User;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserResponseDto {
  private Long id;
  private String username;
  private String nickname;
  private Role role;
  private List<PostingSimpleResponseDto> authoredPostings;
  private List<PostingSimpleResponseDto> joinedPostings;

  public static UserResponseDto of(
      User user,
      List<PostingSimpleResponseDto> authoredPostings,
      List<PostingSimpleResponseDto> joinedPostings) {
    return UserResponseDto.builder()
        .id(user.getId())
        .username(user.getUsername())
        .nickname(user.getNickname())
        .role(user.getRole())
        .authoredPostings(authoredPostings)
        .joinedPostings(joinedPostings)
        .build();
  }
}
