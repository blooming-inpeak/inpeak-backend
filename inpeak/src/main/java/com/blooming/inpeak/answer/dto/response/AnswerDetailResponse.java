package com.blooming.inpeak.answer.dto.response;

import com.blooming.inpeak.answer.domain.Answer;
import com.blooming.inpeak.answer.domain.AnswerStatus;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record AnswerDetailResponse(
    LocalDate dateTime,
    String questionContent,
    Long runningTime,
    AnswerStatus answerStatus,
    boolean isUnderstood,
    String userAnswer,
    String comment,
    String videoUrl,
    String AIAnswer
) {

    public static AnswerDetailResponse from(Answer answer) {
        return AnswerDetailResponse.builder()
            .dateTime(answer.getInterview().getStartDate())  // 답변 작성 시간
            .questionContent(answer.getQuestion().getContent())  // 질문 제목
            .runningTime(answer.getRunningTime())  // 실행 시간
            .answerStatus(answer.getStatus())  // 답변 상태
            .isUnderstood(answer.isUnderstood())  // 이해 여부
            .userAnswer(
                answer.getStatus() == AnswerStatus.SKIPPED ? answer.getQuestion().getBestAnswer()
                    : answer.getUserAnswer())  // 답변 내용
            .comment(answer.getComment())  // 답변 코멘트
            .videoUrl(answer.getVideoURL())  // 비디오 URL
            .AIAnswer(answer.getAIAnswer())  // AI 답변 내용
            .build();
    }
}
