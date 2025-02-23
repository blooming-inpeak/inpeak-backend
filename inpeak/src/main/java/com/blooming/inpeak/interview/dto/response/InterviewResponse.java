package com.blooming.inpeak.interview.dto.response;

public record InterviewResponse(Long id) {

    public static InterviewResponse of(Long id) {
        return new InterviewResponse(id);
    }
}
