package com.blooming.inpeak.member.dto;

import com.blooming.inpeak.member.domain.Member;
import com.blooming.inpeak.member.domain.OAuth2Provider;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public record MemberPrincipal(
    Long id,
    Long kakaoId,
    String nickname,
    OAuth2Provider provider,
    Long totalQuestionCount,
    Long correctAnswerCount,
    Collection<? extends GrantedAuthority> authorities,
    Map<String, Object> attributes
) implements OAuth2User {

    @Builder
    public MemberPrincipal {
    }

    public static MemberPrincipal create(Member member, Map<String, Object> attributes) {
        return MemberPrincipal.builder()
            .id(member.getId())
            .kakaoId(member.getKakaoId())
            .nickname(member.getNickname())
            .provider(member.getProvider())
            .authorities(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")))
            .attributes(attributes)
            .build();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }
}
