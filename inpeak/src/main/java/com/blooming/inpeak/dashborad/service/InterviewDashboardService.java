package com.blooming.inpeak.dashborad.service;

import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.answer.dto.response.MemberLevelResponse;
import com.blooming.inpeak.answer.dto.response.RecentAnswerListResponse;
import com.blooming.inpeak.answer.dto.response.RecentAnswerResponse;
import com.blooming.inpeak.answer.service.AnswerService;
import com.blooming.inpeak.dashborad.dto.InterviewDashboardResponse;
import com.blooming.inpeak.dashborad.dto.SuccessRateResponse;
import com.blooming.inpeak.interview.dto.response.RemainingInterviewsResponse;
import com.blooming.inpeak.interview.service.InterviewService;
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
    private final SuccessRateService successRateService;

    public InterviewDashboardResponse getDashboard(Long memberId, LocalDate startDate) {
        RemainingInterviewsResponse remainingInterviews =
            interviewService.getRemainingInterviews(memberId, startDate);
        MemberLevelResponse levelInfo = answerService.getMemberLevel(memberId);
        RecentAnswerListResponse recentAnswerList =
            answerService.getRecentAnswers(memberId, AnswerStatus.ALL);
        List<RecentAnswerResponse> recentAnswers = recentAnswerList.recentAnswers();
        SuccessRateResponse successRate = successRateService.getSuccessRate(memberId);

        return InterviewDashboardResponse.of(
            remainingInterviews, successRate, levelInfo, recentAnswers);
    }
}
