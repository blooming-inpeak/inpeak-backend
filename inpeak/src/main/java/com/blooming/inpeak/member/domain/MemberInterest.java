package com.blooming.inpeak.member.domain;

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
@Table(name = "member_interests")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterestType interestType;

    @Builder
    private MemberInterest(Long memberId, InterestType interestType) {
        this.memberId = memberId;
        this.interestType = interestType;
    }

    public static MemberInterest of(Long memberId, InterestType interestType) {
        return MemberInterest.builder()
            .memberId(memberId)
            .interestType(interestType)
            .build();
    }
}
