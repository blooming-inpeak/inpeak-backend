package com.blooming.inpeak.answer.dto.response;

import com.blooming.inpeak.answer.domain.Answer;
import com.blooming.inpeak.answer.domain.AnswerStatus;
import lombok.Builder;

@Builder
public record RecentAnswerResponse(
    Long interviewId,
    Long questionId,
    Long answerId,
    String questionContent,
    String answerContent,
    AnswerStatus answerStatus
) {

    public static RecentAnswerResponse from(Answer answer) {
        return RecentAnswerResponse.builder()
            .interviewId(answer.getInterviewId())
            .questionId(answer.getQuestionId())
            .answerId(answer.getId())
            .questionContent(answer.getQuestion().getContent())
            .answerContent(answer.getUserAnswer())
            .answerStatus(answer.getStatus())
            .build();
    }
}
