package com.dangbun.domain.user.client;

import com.dangbun.domain.user.dto.response.KakaoUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "kakao-api-client", url = "https://kapi.kakao.com")
public interface KakaoApiClient {

    /**
     * 카카오 사용자 정보 가져오기
     * @param bearerToken "Bearer {access_token}" 형식의 헤더
     * @return KakaoUserResponse (사용자 프로필 및 계정 정보)
     */
    @GetMapping(value = "/v2/user/me", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    KakaoUserResponse getUserInfo(@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken,
                                  @RequestParam("propertyKeys") String propertyKeys);

    @GetMapping(value = "/v1/user/logout")
    void logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken);
}
