package com.blooming.inpeak.answer.dto.command;

import lombok.Builder;

@Builder
public record AnswerCreateCommand (
    String audioFile,
    Long time,
    Long memberId,
    Long questionId,
    Long interviewId,
    String videoURL
){

}
