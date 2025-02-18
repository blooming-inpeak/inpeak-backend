package com.blooming.inpeak.interview.dto.response;

public record TotalInterviewsResponse(
    long totalCount
) {

    public static TotalInterviewsResponse of(long totalCount) {
        return new TotalInterviewsResponse(totalCount);
    }
}
