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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long kakaoId;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column
    private Long totalQuestionCount;

    @Column
    private Long correctAnswerCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OAuth2Provider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus registrationStatus;

    @Column(nullable = false)
    private String kakaoEmail;

    @Builder
    private Member(
        Long id,
        Long kakaoId, String nickname, Long totalQuestionCount,
        Long correctAnswerCount, OAuth2Provider provider,
        RegistrationStatus registrationStatus, String kakaoEmail
    ) {
        this.id = id;
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.totalQuestionCount = totalQuestionCount;
        this.correctAnswerCount = correctAnswerCount;
        this.provider = provider;
        this.registrationStatus =
            registrationStatus != null ? registrationStatus : RegistrationStatus.INITIATED;
        this.kakaoEmail = kakaoEmail;
    }

    // 테스트 파일에서 사용할 생성자
    @Builder(access = AccessLevel.PRIVATE)
    public Member(
        Long id, Long kakaoId, String nickname,
        OAuth2Provider provider, RegistrationStatus registrationStatus,
        String kakaoEmail
    ) {
        this.id = id;
        this.kakaoId= kakaoId;
        this.nickname = nickname;
        this.provider = provider;
        this.registrationStatus =
            registrationStatus != null ? registrationStatus : RegistrationStatus.INITIATED;
        this.kakaoEmail = kakaoEmail;
    }

    public static Member of(
        Long kakaoId, String nickname, String kakaoEmail,
        OAuth2Provider provider, RegistrationStatus registrationStatus
    ) {
        return Member.builder()
            .kakaoId(kakaoId)
            .nickname(nickname)
            .provider(provider)
            .registrationStatus(
                registrationStatus != null ? registrationStatus : RegistrationStatus.INITIATED)
            .kakaoEmail(kakaoEmail)
            .build();
    }

    public boolean registrationCompleted() {
        return registrationStatus == RegistrationStatus.COMPLETED;
    }

    public void completeRegistration() {
        this.registrationStatus = RegistrationStatus.COMPLETED;
    }

    public void updateNickname(String nickName) {
        this.nickname = nickName;
    }
}
