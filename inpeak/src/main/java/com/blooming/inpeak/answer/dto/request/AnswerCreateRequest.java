package com.blooming.inpeak.answer.dto.request;

import com.blooming.inpeak.answer.dto.command.AnswerCreateCommand;
import jakarta.validation.constraints.NotNull;

public record AnswerCreateRequest (
    @NotNull
    String audioFile,
    @NotNull
    Long time,
    @NotNull
    Long questionId,
    @NotNull
    Long interviewId,
    String videoURL
){
    public AnswerCreateCommand toCommand(Long memberId) {
        return AnswerCreateCommand.builder()
            .audioFile(audioFile)
            .memberId(memberId)
            .interviewId(interviewId)
            .questionId(questionId)
            .time(time)
            .videoURL(videoURL)
            .build();
    }
}
