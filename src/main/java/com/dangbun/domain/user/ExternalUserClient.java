package com.dangbun.domain.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;


@FeignClient(name = "kakao-auth-api", url = "https://kauth.kakao.com/oauth")
public interface ExternalUserClient {

    @GetMapping("/authorize")
    KakaoAuthResponse fetchKakaoAuthCode(@RequestParam String code, @RequestParam String client_id, @RequestParam String redirect_uri);

    @PostMapping(value = "/token", consumes = "application/x-www-form-urlencoded;charset=utf-8")
    KakaoTokenResponse fetchKakaoToken(@RequestParam("grant_type") String grantType,
                                       @RequestParam("client_id") String clientId,
                                       @RequestParam("redirect_uri") String redirectUri,
                                       @RequestParam("code") String code,
                                       @RequestParam("client_secret") String clientSecret);
}
