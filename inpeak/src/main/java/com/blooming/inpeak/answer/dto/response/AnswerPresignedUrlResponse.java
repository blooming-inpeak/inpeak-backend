package com.blooming.inpeak.answer.dto.response;

import lombok.Builder;

@Builder
public record AnswerPresignedUrlResponse(
    String audioUrl,
    String videoUrl
) {

    public static AnswerPresignedUrlResponse of(String audioUrl, String videoUrl) {
        return AnswerPresignedUrlResponse.builder()
            .audioUrl(audioUrl)
            .videoUrl(videoUrl)
            .build();
    }
}
