package com.blooming.inpeak.member.domain;

import com.blooming.inpeak.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Collection;
import java.util.Collections;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Getter
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String accessToken;

    @Column
    private Long totalQuestionCount;

    @Column
    private Long correctAnswerCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OAuth2Provider provider;

    @Builder
    private Member(
        String email, String nickname, String accessToken,
        Long totalQuestionCount, Long correctAnswerCount, OAuth2Provider provider
    ) {
        this.email = email;
        this.nickname = nickname;
        this.accessToken = accessToken;
        this.totalQuestionCount = totalQuestionCount;
        this.correctAnswerCount = correctAnswerCount;
        this.provider = provider;
    }

    // 테스트 파일에서 사용할 생성자
    @Builder(access = AccessLevel.PRIVATE)
    public Member(
        Long id, String email, String nickname, String accessToken,
        OAuth2Provider provider
    ) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.accessToken = accessToken;
        this.provider = provider;
    }

    public static Member of(String email, String nickname, String accessToken, OAuth2Provider provider) {
        return Member.builder()
            .email(email)
            .nickname(nickname)
            .accessToken(accessToken)
            .provider(provider)
            .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return email;
    }
}
