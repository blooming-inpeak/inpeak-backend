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
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewDashboardService {

    private static final Duration DASHBOARD_TTL = Duration.ofSeconds(60); // TTL 설정

    private final InterviewService interviewService;
    private final AnswerService answerService;
    private final SuccessRateService successRateService;
    private final RedisTemplate<String, InterviewDashboardResponse> interviewDashboardRedisTemplate;


    public InterviewDashboardResponse getDashboard(Long memberId, LocalDate startDate) {
        String key = getRedisKey(memberId);

        // Redis 캐시 먼저 조회
        InterviewDashboardResponse cached = interviewDashboardRedisTemplate.opsForValue().get(key);
        if (cached != null) return cached;

        // 원래 로직 수행
        RemainingInterviewsResponse remainingInterviews =
            interviewService.getRemainingInterviews(memberId, startDate);

        MemberLevelResponse levelInfo = answerService.getMemberLevel(memberId);
        List<RecentAnswerResponse> recentAnswers =
            answerService.getRecentAnswers(memberId, AnswerStatus.ALL).recentAnswers();

        SuccessRateResponse successRate = successRateService.getSuccessRate(memberId);

        InterviewDashboardResponse result = InterviewDashboardResponse.of(
            remainingInterviews, successRate, levelInfo, recentAnswers);

        // Redis 캐시에 저장
        interviewDashboardRedisTemplate.opsForValue().set(key, result, DASHBOARD_TTL);

        return result;
    }

    private String getRedisKey(Long memberId) {
        return "dashboard:" + memberId;
    }
}
