package com.blooming.inpeak.answer.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record RecentAnswerListResponse(List<RecentAnswerResponse> recentAnswers) {

    public static RecentAnswerListResponse from(List<RecentAnswerResponse> recentAnswers) {
        return RecentAnswerListResponse.builder()
            .recentAnswers(recentAnswers)
            .build();
    }
}
