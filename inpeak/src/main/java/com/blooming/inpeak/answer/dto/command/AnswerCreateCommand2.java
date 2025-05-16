package com.blooming.inpeak.answer.dto.command;

import lombok.Builder;

@Builder
public record AnswerCreateCommand2 (
    String userAnswer,
    Long time,
    Long memberId,
    Long questionId,
    Long interviewId,
    String videoURL
){

}
