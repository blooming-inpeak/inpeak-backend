package com.blooming.inpeak.answer.controller.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnswerSkipRequest {
    private String questionId;
    private String interviewId;
}
