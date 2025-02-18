package com.blooming.inpeak.interview.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InterviewTest {

    @Test
    @DisplayName("인터뷰 엔티티 생성 테스트")
    void createInterview() {
        // given
        Long memberId = 1L;
        LocalDate startDate = LocalDate.now();

        // when
        Interview interview = Interview.of(memberId, startDate);

        // then
        assertThat(interview.getMemberId()).isEqualTo(memberId);
        assertThat(interview.getStartDate()).isEqualTo(startDate);
    }
}
