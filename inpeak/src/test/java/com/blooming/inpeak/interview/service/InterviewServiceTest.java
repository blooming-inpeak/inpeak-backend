package com.blooming.inpeak.interview.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.blooming.inpeak.interview.domain.Interview;
import com.blooming.inpeak.interview.dto.response.CalendarResponse;
import com.blooming.inpeak.interview.dto.response.RemainingInterviewsResponse;
import com.blooming.inpeak.interview.repository.InterviewRepository;
import com.blooming.inpeak.support.IntegrationTestSupport;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("InterviewService 테스트")
class InterviewServiceTest extends IntegrationTestSupport {

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private InterviewService interviewService;

    private static final Long MEMBER_ID = 1L;

    @Test
    @Transactional
    @DisplayName("회원이 오늘 인터뷰를 진행하지 않았다면 남은 인터뷰 횟수는 1이어야 한다.")
    void getRemainingInterviews_ShouldReturnOneIfNoInterviewToday() {
        // given
        LocalDate today = LocalDate.now();

        // when
        RemainingInterviewsResponse response = interviewService.getRemainingInterviews(MEMBER_ID, today);

        // then
        assertThat(response.count()).isEqualTo(1);
    }

    @Test
    @Transactional
    @DisplayName("회원이 오늘 인터뷰를 이미 진행했다면 남은 인터뷰 횟수는 0이어야 한다.")
    void getRemainingInterviews_ShouldReturnZeroIfInterviewToday() {
        // given
        LocalDate today = LocalDate.now();
        interviewRepository.save(Interview.of(MEMBER_ID, today));

        // when
        RemainingInterviewsResponse response = interviewService.getRemainingInterviews(MEMBER_ID, today);

        // then
        assertThat(response.count()).isEqualTo(0);
    }

    @Test
    @Transactional
    @DisplayName("회원의 특정 월 인터뷰 기록을 조회하면 정확한 데이터를 반환해야 한다.")
    void getCalendar_ShouldReturnInterviewsForSpecificMonth() {
        // given (2월에 인터뷰 3개 추가)
        interviewRepository.save(Interview.of(MEMBER_ID, LocalDate.of(2025, 2, 5)));
        interviewRepository.save(Interview.of(MEMBER_ID, LocalDate.of(2025, 2, 10)));
        interviewRepository.save(Interview.of(MEMBER_ID, LocalDate.of(2025, 2, 15)));

        // when
        List<CalendarResponse> response = interviewService.getCalendar(MEMBER_ID, 2, 2025);

        // then
        assertThat(response).hasSize(3); // 3개가 조회되어야 함
        assertThat(response)
            .extracting(CalendarResponse::date)
            .containsExactlyInAnyOrder(
                LocalDate.of(2025, 2, 5),
                LocalDate.of(2025, 2, 10),
                LocalDate.of(2025, 2, 15)
            );
    }

    @Test
    @Transactional
    @DisplayName("회원이 해당 월에 인터뷰를 진행하지 않았다면 빈 리스트를 반환해야 한다.")
    void getCalendar_ShouldReturnEmptyList_WhenNoInterviews() {
        //given
        interviewRepository.save(Interview.of(MEMBER_ID, LocalDate.of(2025, 2, 5)));
        interviewRepository.save(Interview.of(MEMBER_ID, LocalDate.of(2025, 2, 10)));

        // when
        List<CalendarResponse> response = interviewService.getCalendar(MEMBER_ID, 1, 2025);

        // then
        assertThat(response).isEmpty(); // 인터뷰 기록이 없으므로 빈 리스트 반환
    }
}
