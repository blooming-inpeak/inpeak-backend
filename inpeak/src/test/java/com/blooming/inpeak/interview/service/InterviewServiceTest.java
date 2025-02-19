package com.blooming.inpeak.interview.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.blooming.inpeak.IntegrationTestSupport;
import com.blooming.inpeak.interview.domain.Interview;
import com.blooming.inpeak.interview.dto.response.RemainingInterviewsResponse;
import com.blooming.inpeak.interview.dto.response.TotalInterviewsResponse;
import com.blooming.inpeak.interview.repository.InterviewRepository;
import java.time.LocalDate;
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
    @DisplayName("회원의 총 인터뷰 횟수를 조회하면 정확한 개수를 반환해야 한다.")
    void getTotalInterviews_ShouldReturnCorrectCount() {
        // given
        interviewRepository.save(Interview.of(MEMBER_ID, LocalDate.now().minusDays(1)));
        interviewRepository.save(Interview.of(MEMBER_ID, LocalDate.now().minusDays(2)));

        // when
        TotalInterviewsResponse response = interviewService.getTotalInterviews(MEMBER_ID);

        // then
        assertThat(response.totalCount()).isEqualTo(2);
    }

    @Test
    @Transactional
    @DisplayName("회원이 오늘 인터뷰를 진행하지 않았다면 남은 인터뷰 횟수는 1이어야 한다.")
    void getRemainingInterviews_ShouldReturnOneIfNoInterviewToday() {
        // when
        RemainingInterviewsResponse response = interviewService.getRemainingInterviews(MEMBER_ID);

        // then
        assertThat(response.remainingInterviews()).isEqualTo(1);
    }

    @Test
    @Transactional
    @DisplayName("회원이 오늘 인터뷰를 이미 진행했다면 남은 인터뷰 횟수는 0이어야 한다.")
    void getRemainingInterviews_ShouldReturnZeroIfInterviewToday() {
        // given
        interviewRepository.save(Interview.of(MEMBER_ID, LocalDate.now()));

        // when
        RemainingInterviewsResponse response = interviewService.getRemainingInterviews(MEMBER_ID);

        // then
        assertThat(response.remainingInterviews()).isEqualTo(0);
    }
}
