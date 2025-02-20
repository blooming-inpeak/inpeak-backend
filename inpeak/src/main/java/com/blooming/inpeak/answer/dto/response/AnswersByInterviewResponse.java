package com.blooming.inpeak.answer.dto.response;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Builder;

@Builder
public record AnswersByInterviewResponse(
    Long interviewId,
    List<Long> answerIds
) {
    public static List<AnswersByInterviewResponse> from (Map<Long, List<Long>> map) {
        return map.entrySet().stream()
            .map(entry -> new AnswersByInterviewResponse(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }
}

