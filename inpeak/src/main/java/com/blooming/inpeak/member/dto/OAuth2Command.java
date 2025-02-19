package com.blooming.inpeak.member.dto;

import com.blooming.inpeak.member.domain.OAuth2Provider;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuth2Command {

    private String email;
    private OAuth2Provider provider;

    @Builder
    private OAuth2Command(String email, OAuth2Provider provider) {
        this.email = email;
        this.provider = provider;
    }

    public static OAuth2Command of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("kakao".equals(registrationId)) {
            return ofKakao(userNameAttributeName, attributes);
        }
        if ("google".equals(registrationId)) {
            return ofGoogle(userNameAttributeName, attributes);
        }
        if ("naver".equals(registrationId)) {
            return ofNaver(userNameAttributeName, attributes);
        }

        throw new IllegalArgumentException("지원하지 않는 소셜 로그인 제공자입니다: " + registrationId);
    }

    @SuppressWarnings("unchecked")
    private static OAuth2Command ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        String email = (String) kakaoAccount.get("email");

        return OAuth2Command.builder()
            .email(email)
            .provider(OAuth2Provider.KAKAO)
            .build();
    }

    private static OAuth2Command ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        String email = (String) attributes.get("email");

        return OAuth2Command.builder()
            .email(email)
            .provider(OAuth2Provider.GOOGLE)
            .build();
    }

    @SuppressWarnings("unchecked")
    private static OAuth2Command ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        String email = (String) response.get("email");

        return OAuth2Command.builder()
            .email(email)
            .provider(OAuth2Provider.NAVER)
            .build();
    }
}
