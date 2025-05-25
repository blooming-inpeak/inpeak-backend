package com.blooming.inpeak.member.domain;

import com.blooming.inpeak.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "member_statistics")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberStatistics extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private int correctCount;

    @Column(nullable = false)
    private int incorrectCount;

    @Column(nullable = false)
    private int skippedCount;

    // total_count는 GENERATED 컬럼이므로 insert/update 하지 않음
    @Column(insertable = false, updatable = false)
    private int totalCount;

    @Builder
    private MemberStatistics(
        Long memberId,
        int correctCount,
        int incorrectCount,
        int skippedCount
    ) {
        this.memberId = memberId;
        this.correctCount = correctCount;
        this.incorrectCount = incorrectCount;
        this.skippedCount = skippedCount;
    }

    public static MemberStatistics of(Long memberId) {
        return MemberStatistics.builder()
            .memberId(memberId)
            .correctCount(0)
            .incorrectCount(0)
            .skippedCount(0)
            .build();
    }

    // 통계 수치 증가 메서드
    public void increaseCorrect() {
        this.correctCount++;
    }

    public void increaseIncorrect() {
        this.incorrectCount++;
    }

    public void increaseSkipped() {
        this.skippedCount++;
    }
}
