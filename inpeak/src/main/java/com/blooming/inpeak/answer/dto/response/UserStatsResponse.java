package com.blooming.inpeak.answer.dto.response;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record UserStatsResponse(
    Long totalAnswerCount,
    Long correctAnswerCount,
    Long incorrectAnswerCount,
    Long skippedAnswerCount,
    Long totalInterviewCount,
    Long totalRunningTime
) {
    // 기존 생성자에서 필드 값을 직접 할당하도록 수정
    public UserStatsResponse(
        Long totalAnswerCount,
        Long correctAnswerCount,
        Long incorrectAnswerCount,
        Long skippedAnswerCount,
        Long totalInterviewCount,
        Number totalRunningTime  // ✅ Integer, BigDecimal, Long 모두 받을 수 있음
    ) {
        this(
            totalAnswerCount,
            correctAnswerCount == null ? 0 : correctAnswerCount,
            incorrectAnswerCount == null ? 0 : incorrectAnswerCount,
            skippedAnswerCount == null ? 0 : skippedAnswerCount,
            totalInterviewCount,
            convertToLong(totalRunningTime) // ✅ 변환 로직을 주 생성자로 넘김
        );
    }

    private static Long convertToLong(Number value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).longValue(); // ✅ BigDecimal 지원
        }
        return value.longValue();
    }
}
