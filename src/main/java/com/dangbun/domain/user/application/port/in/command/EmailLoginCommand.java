package com.dangbun.domain.user.application.port.in.command;

import lombok.Value;

@Value
public class EmailLoginCommand {
    String email;
    String password;
}
