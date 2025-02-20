package com.blooming.inpeak.interview.dto.response;

import com.blooming.inpeak.interview.domain.Interview;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record CalendarResponse(
    LocalDate date,
    Long interviewId
) {
    public static CalendarResponse from(Interview interview) {
        return CalendarResponse.builder()
            .date(interview.getStartDate())
            .interviewId(interview.getId())
            .build();
    }
}
