package com.dangbun.domain.user.application.port.in.command;

import lombok.Value;

@Value
public class SignupCommand {
    String name;
    String email;
    String password;
    String certCode;
}
