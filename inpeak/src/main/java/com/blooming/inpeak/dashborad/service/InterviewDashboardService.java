package com.blooming.inpeak.dashborad.service;

import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.member.dto.response.MemberLevelResponse;
import com.blooming.inpeak.answer.dto.response.RecentAnswerResponse;
import com.blooming.inpeak.answer.service.AnswerService;
import com.blooming.inpeak.dashborad.dto.InterviewDashboardResponse;
import com.blooming.inpeak.member.dto.response.SuccessRateResponse;
import com.blooming.inpeak.interview.dto.response.RemainingInterviewsResponse;
import com.blooming.inpeak.interview.service.InterviewService;
import com.blooming.inpeak.member.service.MemberStatisticsService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewDashboardService {

    private final InterviewService interviewService;
    private final AnswerService answerService;
    private final MemberStatisticsService memberStatisticsService;

    public InterviewDashboardResponse getDashboard(Long memberId, LocalDate startDate) {
        RemainingInterviewsResponse remainingInterviews =
            interviewService.getRemainingInterviews(memberId, startDate);

        MemberLevelResponse levelInfo = memberStatisticsService.getMemberLevel(memberId);
        SuccessRateResponse successRate = memberStatisticsService.getSuccessRate(memberId);
        List<RecentAnswerResponse> recentAnswers =
            answerService.getRecentAnswers(memberId, AnswerStatus.ALL).recentAnswers();

        return InterviewDashboardResponse.of(
            remainingInterviews, successRate, levelInfo, recentAnswers);
    }
}
