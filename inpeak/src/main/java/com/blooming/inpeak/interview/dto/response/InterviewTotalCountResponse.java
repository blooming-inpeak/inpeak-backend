package com.blooming.inpeak.interview.dto.response;

public record InterviewTotalCountResponse(
    long totalCount
) {

    public static InterviewTotalCountResponse of(long totalCount) {
        return new InterviewTotalCountResponse(totalCount);
    }
}
