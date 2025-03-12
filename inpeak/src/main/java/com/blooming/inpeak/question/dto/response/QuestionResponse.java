package com.blooming.inpeak.question.dto.response;

import com.blooming.inpeak.question.domain.Question;
import lombok.Builder;

@Builder
public record QuestionResponse(Long id, String content) {

    public static QuestionResponse from(Question question) {
        return QuestionResponse.builder()
            .id(question.getId())
            .content(question.getContent())
            .build();
    }
}