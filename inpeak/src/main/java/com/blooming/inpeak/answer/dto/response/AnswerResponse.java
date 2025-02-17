package com.blooming.inpeak.answer.dto.response;

import com.blooming.inpeak.answer.domain.Answer;
import com.blooming.inpeak.answer.domain.AnswerStatus;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record AnswerResponse(
    LocalDate dateTime,
    String questionTitle,
    Long runningTime,
    AnswerStatus answerStatus,
    boolean isUnderstood
) {
    public static AnswerResponse from (Answer answer) {
        return AnswerResponse.builder()
            .dateTime(answer.getInterview().getStartDate())  // 답변 작성 시간
            .questionTitle(answer.getQuestion().getTitle())  // 질문 제목
            .runningTime(answer.getRunningTime())  // 실행 시간
            .answerStatus(answer.getStatus())  // 답변 상태
            .isUnderstood(answer.isUnderstood())  // 이해 여부
            .build();
    }
}
