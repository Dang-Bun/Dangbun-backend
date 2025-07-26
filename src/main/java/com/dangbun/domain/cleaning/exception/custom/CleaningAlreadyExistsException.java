package com.dangbun.domain.cleaning.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class CleaningAlreadyExistsException extends RuntimeException {
  private final ResponseStatus exceptionStatus;

  public CleaningAlreadyExistsException(ResponseStatus exceptionStatus) {
    super(exceptionStatus.getMessage());
    this.exceptionStatus = exceptionStatus;
  }
}