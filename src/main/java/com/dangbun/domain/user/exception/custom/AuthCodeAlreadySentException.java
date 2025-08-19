package com.dangbun.domain.user.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class AuthCodeAlreadySentException extends RuntimeException {
  private final ResponseStatus exceptionStatus;

  public AuthCodeAlreadySentException(ResponseStatus status) {
    super(status.getMessage());
    this.exceptionStatus = status;
  }}
