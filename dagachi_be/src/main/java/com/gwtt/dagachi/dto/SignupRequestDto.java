package com.gwtt.dagachi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignupRequestDto {
  @NotBlank(message = "사용자 ID는 필수 입력 항목입니다.")
  @Size(min = 4, max = 20, message = "사용자 ID는 4자 이상 20자 이하여야 합니다.")
  private String username;

  @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
  @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
  private String password;

  @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
  private String nickname;
}
