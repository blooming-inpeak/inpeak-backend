package com.blooming.inpeak.interview.dto.response;

import lombok.Builder;

@Builder
public record RemainingInterviewsResponse(
    int count
) {
    public static RemainingInterviewsResponse of(int count) {
        return RemainingInterviewsResponse.builder()
            .count(count)
            .build();
    }
}
