package com.dangbun.domain.member.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class DutyNotInPlaceException extends RuntimeException {

  private final ResponseStatus exceptionStatus;

  public DutyNotInPlaceException(ResponseStatus status) {
    super(status.getMessage());
    this.exceptionStatus = status;
  }
}