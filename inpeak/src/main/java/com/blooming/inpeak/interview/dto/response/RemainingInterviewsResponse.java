package com.blooming.inpeak.interview.dto.response;

public record RemainingInterviewsResponse(
    int remainingInterviews
) {
    public static RemainingInterviewsResponse of(int remainingInterviews) {
        return new RemainingInterviewsResponse(remainingInterviews);
    }
}
