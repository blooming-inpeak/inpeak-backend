package com.blooming.inpeak.answer.dto.response;

import com.blooming.inpeak.answer.domain.Answer;
import com.blooming.inpeak.answer.domain.AnswerStatus;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record AnswerResponse(
    Long answerId,
    LocalDate dateTime,
    String questionContent,
    Long runningTime,
    AnswerStatus answerStatus,
    boolean isUnderstood
) {
    public static AnswerResponse from (Answer answer) {
        return AnswerResponse.builder()
            .answerId(answer.getId())  // 답변 ID
            .dateTime(answer.getInterview().getStartDate())  // 답변 작성 시간
            .questionContent(answer.getQuestion().getContent())  // 질문 제목
            .runningTime(answer.getRunningTime())  // 실행 시간
            .answerStatus(answer.getStatus())  // 답변 상태
            .isUnderstood(answer.isUnderstood())  // 이해 여부
            .build();
    }
}
