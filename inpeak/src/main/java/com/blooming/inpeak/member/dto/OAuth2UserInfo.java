package com.blooming.inpeak.member.dto;

import com.blooming.inpeak.member.domain.OAuth2Provider;
import java.util.Map;
import org.springframework.security.oauth2.core.user.OAuth2User;

public record OAuth2UserInfo(
    String email,
    OAuth2Provider provider
) {
    public static OAuth2UserInfo from(OAuth2User oauth2User, OAuth2Provider provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        if (kakaoAccount == null || !kakaoAccount.containsKey("email")) {
            throw new RuntimeException("카카오 계정에서 이메일을 가져올 수 없습니다.");
        }

        return new OAuth2UserInfo(
            (String) kakaoAccount.get("email"),
            provider
        );
    }
}
