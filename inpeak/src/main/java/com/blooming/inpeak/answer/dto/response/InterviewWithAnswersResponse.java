package com.blooming.inpeak.answer.dto.response;

import com.blooming.inpeak.answer.domain.Answer;
import com.blooming.inpeak.interview.domain.Interview;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record InterviewWithAnswersResponse(
    ZonedDateTime createdAt, // 인터뷰 생성 시간
    LocalDate startDate, // 인터뷰 시작 날짜 (시간 포함)
    List<AnswerResponse> answers // 답변 리스트
) {
    public static InterviewWithAnswersResponse from(Interview interview, List<Answer> answers) {
        return InterviewWithAnswersResponse.builder()
            .createdAt(interview.getCreatedAt())
            .startDate(interview.getStartDate())
            .answers(answers.stream().map(AnswerResponse::from).toList())
            .build();
    }
}
