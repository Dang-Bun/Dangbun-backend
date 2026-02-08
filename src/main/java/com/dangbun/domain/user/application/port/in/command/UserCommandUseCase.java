package com.dangbun.domain.user.application.port.in.command;

public interface UserCommandUseCase {

    void sendSignupAuthCode(String email);

    void signup(SignupCommand command);

    void updatePassword(UpdatePasswordCommand command);

    void deleteUser(DeleteUserCommand command);
}
