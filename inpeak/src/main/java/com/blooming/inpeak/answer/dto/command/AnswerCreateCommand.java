package com.blooming.inpeak.answer.dto.command;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record AnswerCreateCommand (
    MultipartFile audioFile,
    Long time,
    Long memberId,
    Long questionId,
    Long interviewId,
    String videoURL
){
    public static AnswerCreateCommand of(
        MultipartFile audioFile,
        Long time,
        Long memberId,
        Long questionId,
        Long interviewId,
        String videoURL
    ) {
        return AnswerCreateCommand
            .builder()
            .audioFile(audioFile)
            .time(time)
            .memberId(memberId)
            .questionId(questionId)
            .interviewId(interviewId)
            .videoURL(videoURL)
            .build();
    }
}
