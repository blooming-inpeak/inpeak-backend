package com.blooming.inpeak.interview.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.blooming.inpeak.interview.dto.response.RemainingInterviewsResponse;
import com.blooming.inpeak.interview.dto.response.TotalInterviewsResponse;
import com.blooming.inpeak.interview.repository.InterviewRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {

    @Mock
    private InterviewRepository interviewRepository;

    @InjectMocks
    private InterviewService interviewService;

    private static final Long MEMBER_ID = 1L;

    @Test
    @DisplayName("회원의 총 인터뷰 횟수를 조회하면 정확한 개수를 반환해야 한다.")
    void getTotalInterviews_ShouldReturnCorrectCount() {
        // given
        when(interviewRepository.countByMemberId(MEMBER_ID)).thenReturn(5L);

        // when
        TotalInterviewsResponse response = interviewService.getTotalInterviews(MEMBER_ID);

        // then
        assertThat(response.totalCount()).isEqualTo(5);
        verify(interviewRepository, times(1)).countByMemberId(MEMBER_ID);
    }

    @Test
    @DisplayName("회원이 오늘 인터뷰를 진행하지 않았다면 남은 인터뷰 횟수는 1이어야 한다.")
    void getRemainingInterviews_ShouldReturnOneIfNoInterviewToday() {
        // given
        when(interviewRepository.existsByMemberIdAndStartDate(MEMBER_ID, LocalDate.now())).thenReturn(false);

        // when
        RemainingInterviewsResponse response = interviewService.getRemainingInterviews(MEMBER_ID);

        // then
        assertThat(response.remainingInterviews()).isEqualTo(1);
        verify(interviewRepository, times(1)).existsByMemberIdAndStartDate(MEMBER_ID, LocalDate.now());
    }

    @Test
    @DisplayName("회원이 오늘 인터뷰를 이미 진행했다면 남은 인터뷰 횟수는 0이어야 한다.")
    void getRemainingInterviews_ShouldReturnZeroIfInterviewToday() {
        // given
        when(interviewRepository.existsByMemberIdAndStartDate(MEMBER_ID, LocalDate.now())).thenReturn(true);

        // when
        RemainingInterviewsResponse response = interviewService.getRemainingInterviews(MEMBER_ID);

        // then
        assertThat(response.remainingInterviews()).isEqualTo(0);
        verify(interviewRepository, times(1)).existsByMemberIdAndStartDate(MEMBER_ID, LocalDate.now());
    }
}
