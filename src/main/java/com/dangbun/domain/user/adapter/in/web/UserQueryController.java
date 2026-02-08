package com.dangbun.domain.user.adapter.in.web;

import com.dangbun.common.hexagonal.WebAdapter;
import com.dangbun.domain.user.adapter.in.web.dto.request.PostUserAuthCodeRequest;
import com.dangbun.domain.user.adapter.in.web.dto.response.GetUserMyInfoResponse;
import com.dangbun.domain.user.application.port.in.query.UserInfoResult;
import com.dangbun.domain.user.application.port.in.query.UserQuery;
import com.dangbun.domain.user.detail.CustomUserDetails;
import com.dangbun.domain.user.response.status.UserExceptionResponse;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@Tag(name = "User", description = "UserController - 회원 관련 API")
@RequiredArgsConstructor
@WebAdapter(path = "/users")
public class UserQueryController {

    private final UserQuery userQuery;

    @Operation(summary = "인증번호 생성(비밀번호 재설정용)", description = "이메일 인증번호를 생성합니다.(비밀번호 재설정 용)")
    @DocumentedApiErrors(
            value = {UserExceptionResponse.class},
            includes = {"INVALID_EMAIL", "AUTH_CODE_SENT"}
    )
    @PostMapping("/email-code")
    public ResponseEntity<BaseResponse<?>> generatePasswordAuthCode(@RequestBody PostUserAuthCodeRequest request) {
        userQuery.sendFindPasswordAuthCode(request.email());
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "로그아웃", description = "로그아웃 시 클라이언트 측에서 bearer token을 삭제해주어야 합니다")
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<?>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader("Authorization") String bearerToken
    ) {
        userQuery.logout(userDetails.getUser().getUserId(), bearerToken);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "내 회원 정보 조회", description = "회원가입 시 입력한 이름, 이메일 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<GetUserMyInfoResponse>> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserInfoResult result = userQuery.getMyInfo(userDetails.getUser().getUserId());
        GetUserMyInfoResponse response = GetUserMyInfoResponse.from(result);
        return ResponseEntity.ok(BaseResponse.ok(response));
    }
}
