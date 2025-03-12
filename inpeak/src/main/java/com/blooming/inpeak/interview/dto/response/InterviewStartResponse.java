package com.blooming.inpeak.interview.dto.response;

import com.blooming.inpeak.question.dto.response.QuestionResponse;
import java.util.List;
import lombok.Builder;

@Builder
public record InterviewStartResponse(
    Long interviewId,
    List<QuestionResponse> questions
) {

    public static InterviewStartResponse of(Long interviewId, List<QuestionResponse> questions) {
        return InterviewStartResponse.builder()
            .interviewId(interviewId)
            .questions(questions)
            .build();
    }

}
