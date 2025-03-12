package com.blooming.inpeak.answer.dto.response;

import lombok.Builder;

@Builder
public record AnswerPresignedUrlResponse(String url) {

    public static AnswerPresignedUrlResponse of(String url) {
        return AnswerPresignedUrlResponse.builder()
            .url(url)
            .build();
    }
}
