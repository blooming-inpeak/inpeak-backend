package com.blooming.inpeak.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Getter
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 15)
    private String nickName;

    @Column(nullable = false, unique = true)
    private String accessToken;

    @Column
    private Long totalQuestionCount;

    @Column
    private Long correctAnswerCount;

    @Builder
    private Member(String nickName, String accessToken) {
        this.nickName = nickName;
        this.accessToken = accessToken;
        this.totalQuestionCount = 0L;
        this.correctAnswerCount = 0L;
    }

    public static Member of(String nickName, String accessToken) {
        return Member.builder()
            .nickName(nickName)
            .accessToken(accessToken)
            .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }
}
