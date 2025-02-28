package com.blooming.inpeak.answer.dto.response;

public record AnswerPresignedUrlResponse(String url) {

    public static AnswerPresignedUrlResponse of(String url) {
        return new AnswerPresignedUrlResponse(url);
    }
}
