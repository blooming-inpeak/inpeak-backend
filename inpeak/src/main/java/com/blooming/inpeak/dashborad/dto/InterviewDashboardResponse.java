package com.blooming.inpeak.dashborad.dto;

import com.blooming.inpeak.answer.dto.response.MemberLevelResponse;
import com.blooming.inpeak.answer.dto.response.RecentAnswerListResponse;
import com.blooming.inpeak.answer.dto.response.RecentAnswerResponse;
import com.blooming.inpeak.interview.dto.response.RemainingInterviewsResponse;
import java.util.List;
import lombok.Builder;

@Builder
public record InterviewDashboardResponse(RemainingInterviewsResponse remainingInterviews,
                                         MemberLevelResponse levelInfo,
                                         List<RecentAnswerResponse> recentAnswers) {

    public static InterviewDashboardResponse of(
        RemainingInterviewsResponse remainingInterviews,
        MemberLevelResponse levelInfo,
        List<RecentAnswerResponse> recentAnswers
    ) {
        return InterviewDashboardResponse.builder()
            .remainingInterviews(remainingInterviews)
            .levelInfo(levelInfo)
            .recentAnswers(recentAnswers)
            .build();
    }
}
