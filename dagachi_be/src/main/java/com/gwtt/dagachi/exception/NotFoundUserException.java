package com.gwtt.dagachi.exception;

public class NotFoundUserException extends RuntimeException {
  public NotFoundUserException(String message) {
    super(message);
  }
}
