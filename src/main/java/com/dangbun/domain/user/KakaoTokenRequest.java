package com.dangbun.domain.user;

public record KakaoTokenRequest(
        String grant_type,
        String client_id,
        String redirect_uri,
        String code,
        String client_secret
) {
    public static KakaoTokenRequest of(String grant_type, String client_id, String redirect_uri, String code, String client_secret){
        return new KakaoTokenRequest(grant_type, client_id, redirect_uri, code, client_secret);
    }
}
