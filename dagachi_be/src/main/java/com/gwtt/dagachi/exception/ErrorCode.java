package com.gwtt.dagachi.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  LOGIN_FAILED(HttpStatus.BAD_REQUEST, "로그인에 실패했습니다."),

  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
  DUPLICATE_USERNAME(HttpStatus.CONFLICT, "이미 존재하는 사용자 ID입니다."),
  DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
  USER_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "해당 사용자는 권한이 없습니다."),

  POSTING_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
  POSTING_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "해당 게시글에 대한 권한이 없습니다."),
  POSTING_ALREADY_RECRUITED(HttpStatus.BAD_REQUEST, "이미 완료되거나 참여가 마감된 게시글입니다."),

  PARTICIPATION_NOT_FOUND(HttpStatus.NOT_FOUND, "참여 정보를 찾을 수 없습니다."),
  PARTICIPATION_ALREADY_APPROVED(HttpStatus.BAD_REQUEST, "이미 승인된 참여 정보입니다."),
  PARTICIPATION_ALREADY_REJECTED(HttpStatus.BAD_REQUEST, "이미 거절된 참여 정보입니다."),
  PARTICIPATION_ALREADY_JOINED(HttpStatus.BAD_REQUEST, "이미 참여한 게시글입니다."),
  PARTICIPATION_MAX_CAPACITY_EXCEEDED(HttpStatus.BAD_REQUEST, "해당 게시글의 최대 참여 인원을 초과했습니다."),

  COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
  COMMENT_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "해당 댓글에 대한 권한이 없습니다."),
  COMMENT_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 댓글입니다."),
  COMMENT_REPLY_TO_DELETED_COMMENT(HttpStatus.BAD_REQUEST, "이미 삭제된 댓글에는 대댓글을 달 수 없습니다."),
  COMMENT_POSTING_NOT_MATCHED(HttpStatus.BAD_REQUEST, "댓글의 게시글과 일치하지 않습니다."),

  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "예기치 않은 오류가 발생했습니다.");

  private final HttpStatus status;
  private final String message;
}
