package com.gwtt.dagachi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginRequestDto {
  @NotBlank(message = "사용자 ID는 필수 입력 항목입니다.")
  private String username;

  @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
  private String password;
}
