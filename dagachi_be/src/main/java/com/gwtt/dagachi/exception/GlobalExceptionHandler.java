package com.gwtt.dagachi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException e) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
  }

  @ExceptionHandler(DagachiException.class)
  public ResponseEntity<ErrorResponse> handleDagachiException(DagachiException e) {
    ErrorCode errorCode = e.getErrorCode();
    ErrorResponse errorResponse =
        ErrorResponse.builder().code(errorCode.name()).message(errorCode.getMessage()).build();
    return ResponseEntity.status(errorCode.getStatus()).body(errorResponse);
  }
}
