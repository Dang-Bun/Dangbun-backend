package com.dangbun.domain.member.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class PlaceNameNotMatchedException extends RuntimeException {
  private final ResponseStatus exceptionStatus;

  public PlaceNameNotMatchedException(ResponseStatus status) {
    super(status.getMessage());
    this.exceptionStatus = status;
  }
}