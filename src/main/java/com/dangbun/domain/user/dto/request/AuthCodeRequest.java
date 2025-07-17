package com.dangbun.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AuthCodeRequest {

    @Email(message = "이메일 형식이 아닙니다")
    @NotEmpty(message = "이메일 필드가 비어있습니다")
    private String email;

}
