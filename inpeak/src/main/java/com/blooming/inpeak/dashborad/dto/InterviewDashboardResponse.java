package com.blooming.inpeak.dashborad.dto;

import com.blooming.inpeak.member.dto.response.MemberLevelResponse;
import com.blooming.inpeak.answer.dto.response.RecentAnswerResponse;
import com.blooming.inpeak.interview.dto.response.RemainingInterviewsResponse;
import com.blooming.inpeak.member.dto.response.SuccessRateResponse;
import java.util.List;
import lombok.Builder;

@Builder
public record InterviewDashboardResponse(RemainingInterviewsResponse remainingInterviews,
                                         SuccessRateResponse successRate,
                                         MemberLevelResponse levelInfo,
                                         List<RecentAnswerResponse> recentAnswers) {

    public static InterviewDashboardResponse of(
        RemainingInterviewsResponse remainingInterviews,
        SuccessRateResponse successRate,
        MemberLevelResponse levelInfo,
        List<RecentAnswerResponse> recentAnswers
    ) {
        return InterviewDashboardResponse.builder()
            .remainingInterviews(remainingInterviews)
            .successRate(successRate)
            .levelInfo(levelInfo)
            .recentAnswers(recentAnswers)
            .build();
    }
}
