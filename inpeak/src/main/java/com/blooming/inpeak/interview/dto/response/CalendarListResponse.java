package com.blooming.inpeak.interview.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record CalendarListResponse(
    List<CalendarResponse> calendarList,
    boolean exists
) {
    public static CalendarListResponse from(List<CalendarResponse> calendarList, boolean exists) {
        return CalendarListResponse.builder()
            .calendarList(calendarList)
            .exists(exists)
            .build();
    }
}
