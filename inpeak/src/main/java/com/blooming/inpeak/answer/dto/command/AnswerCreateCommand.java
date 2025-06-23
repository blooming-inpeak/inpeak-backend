package com.blooming.inpeak.answer.dto.command;

import com.blooming.inpeak.answer.domain.AnswerTask;
import lombok.Builder;

@Builder
public record AnswerCreateCommand (
    String audioURL,
    Long time,
    Long memberId,
    Long questionId,
    Long interviewId,
    String videoURL
){
    public static AnswerCreateCommand from (AnswerTask answerTask) {
        return AnswerCreateCommand
            .builder()
            .audioURL(answerTask.getAudioFileUrl())
            .time(answerTask.getTime())
            .memberId(answerTask.getMemberId())
            .questionId(answerTask.getQuestionId())
            .interviewId(answerTask.getInterviewId())
            .videoURL(answerTask.getVideoUrl())
            .build();
    }
}
