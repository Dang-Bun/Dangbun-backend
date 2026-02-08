package com.dangbun.domain.user.application.port.in.command;

import lombok.Value;

@Value
public class KakaoLoginCommand {
    String code;
    String error;
    String error_description;
    String state;
}
