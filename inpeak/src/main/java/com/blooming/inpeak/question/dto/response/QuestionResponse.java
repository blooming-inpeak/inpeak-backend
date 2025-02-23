package com.blooming.inpeak.question.dto.response;

import com.blooming.inpeak.question.domain.Question;

public record QuestionResponse(Long id, String content) {

    public static QuestionResponse from(Question question) {
        return new QuestionResponse(
            question.getId(),
            question.getContent()
        );
    }
}