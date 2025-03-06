package com.blooming.inpeak.answer.dto.request;

import com.blooming.inpeak.answer.dto.command.AnswerCreateCommand;

public record AnswerCreateRequest (
    String audioFile,
    int time,
    Long questionId,
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
