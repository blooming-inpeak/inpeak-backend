package com.blooming.inpeak.member.domain;

import com.blooming.inpeak.answer.domain.AnswerStatus;
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

    private static final int[] LEVEL_EXP_TABLE = {0, 30, 90, 180, 300, 450, 630, 840, 1080, 1350};
    private static final int MAX_LEVEL = LEVEL_EXP_TABLE.length;

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

    public void increaseCorrect() {
        this.correctCount++;
    }

    public void increaseIncorrect() {
        this.incorrectCount++;
    }

    public void increaseSkipped() {
        this.skippedCount++;
    }

    public int getTotalCount() { return correctCount + incorrectCount + skippedCount; }

    public int calculateExp() {
        return correctCount * AnswerStatus.CORRECT.getExpPoints()
            + incorrectCount * AnswerStatus.INCORRECT.getExpPoints();
    }

    public int calculateLevel(int exp) {
        if (exp == 0) return 0;

        double val = (1 + Math.sqrt(1 + (4.0 * exp / 15.0))) / 2.0;
        int level = (int) Math.floor(val);

        return Math.min(level, MAX_LEVEL);
    }

    public int getCurrentExpInLevel(int exp, int level) {
        return (level == 0) ? 0 : exp - LEVEL_EXP_TABLE[level - 1];
    }

    public int getNextExpInLevel(int level) {
        return (level == MAX_LEVEL) ? 0 : LEVEL_EXP_TABLE[level] - LEVEL_EXP_TABLE[level - 1];
    }
}
