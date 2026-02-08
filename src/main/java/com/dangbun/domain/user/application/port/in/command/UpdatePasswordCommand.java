package com.dangbun.domain.user.application.port.in.command;

import lombok.Value;

@Value
public class UpdatePasswordCommand {
    String email;
    String certCode;
    String password;
}
