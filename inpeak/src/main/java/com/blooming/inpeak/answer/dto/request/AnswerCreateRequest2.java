package com.blooming.inpeak.answer.dto.request;

import com.blooming.inpeak.answer.dto.command.AnswerCreateCommand;
import com.blooming.inpeak.answer.dto.command.AnswerCreateCommand2;
import jakarta.validation.constraints.NotNull;

public record AnswerCreateRequest2 (
    @NotNull
    String userAnswer,
    @NotNull
    Long time,
    @NotNull
    Long questionId,
    @NotNull
    Long interviewId,
    String videoURL
){
    public AnswerCreateCommand2 toCommand(Long memberId) {
        return AnswerCreateCommand2.builder()
            .userAnswer(userAnswer)
            .memberId(memberId)
            .interviewId(interviewId)
            .questionId(questionId)
            .time(time)
            .videoURL(videoURL)
            .build();
    }
}
