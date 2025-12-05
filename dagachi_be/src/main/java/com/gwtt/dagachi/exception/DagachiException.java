package com.gwtt.dagachi.exception;

import lombok.Getter;

@Getter
public class DagachiException extends RuntimeException {
  private final ErrorCode errorCode;

  public DagachiException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
