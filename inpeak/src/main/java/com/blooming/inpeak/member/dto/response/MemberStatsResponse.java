package com.blooming.inpeak.member.dto.response;

import com.blooming.inpeak.member.domain.MemberStatistics;
import lombok.Builder;

@Builder
public record MemberStatsResponse(
    int totalAnswerCount,
    int correctAnswerCount,
    int incorrectAnswerCount,
    int skippedAnswerCount,
    Long totalInterviewCount,
    Long totalRunningTime
) {
    public static MemberStatsResponse of(
        MemberStatistics statistics,
        long interviewCount,
        long totalRunningTime
    ) {
        return MemberStatsResponse.builder()
            .totalAnswerCount(statistics.getTotalCount())
            .correctAnswerCount(statistics.getCorrectCount())
            .incorrectAnswerCount(statistics.getIncorrectCount())
            .skippedAnswerCount(statistics.getSkippedCount())
            .totalInterviewCount(interviewCount)
            .totalRunningTime(totalRunningTime)
            .build();
    }
}