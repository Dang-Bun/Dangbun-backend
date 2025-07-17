package com.dangbun.domain.user.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
@RestControllerAdvice(basePackages = "com.dangbun.domain.user")
public class UserControllerAdvice {


}
