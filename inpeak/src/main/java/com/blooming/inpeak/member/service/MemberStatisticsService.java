package com.blooming.inpeak.member.service;

import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.answer.repository.AnswerRepository;
import com.blooming.inpeak.common.error.exception.NotFoundException;
import com.blooming.inpeak.member.domain.MemberStatistics;
import com.blooming.inpeak.member.dto.response.MemberLevelResponse;
import com.blooming.inpeak.member.dto.response.SuccessRateResponse;
import com.blooming.inpeak.member.dto.response.MemberStatsResponse;
import com.blooming.inpeak.member.repository.MemberStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberStatisticsService {

    private final MemberStatisticsRepository memberStatisticsRepository;
    private final AnswerRepository answerRepository;

    /**
     * 회원 통계 업데이트
     *
     * @param memberId
     * @param status
     */
    @Transactional
    public void updateStatistics(Long memberId, AnswerStatus status) {
        MemberStatistics stats = memberStatisticsRepository.findByMemberId(memberId)
            .orElseThrow(()-> new NotFoundException("회원 통계 데이터를 찾을 수 없습니다."));

        status.applyTo(stats);
    }

    /**
     * 사용자의 답변 통계를 조회하는 메서드
     *
     * @param memberId 사용자 ID
     * @return 사용자의 답변 통계
     */
    public MemberStatsResponse getMemberStats(Long memberId) {
        MemberStatistics stats = memberStatisticsRepository.findByMemberId(memberId)
            .orElseThrow(()-> new NotFoundException("회원 통계 데이터를 찾을 수 없습니다."));

        long interviewCount = answerRepository.countTotalInterviewsByMemberId(memberId);
        long totalRunningTime = answerRepository.sumTotalRunningTimeByMemberId(memberId);

        return MemberStatsResponse.of(stats, interviewCount, totalRunningTime);
    }

    /**
     * 사용자 및 전체 성공률을 조회하는 메서드
     *
     * @param memberId 사용자 ID
     * @return 사용자 및 전체 성공률
     */
    public SuccessRateResponse getSuccessRate(Long memberId) {
        return SuccessRateResponse.of(
            memberStatisticsRepository.getMemberSuccessRate(memberId),
            memberStatisticsRepository.getAverageSuccessRate()
        );
    }

    /**
     * 회원의 레벨 정보를 가져오는 메서드
     *
     * @param memberId 사용자 ID
     * @return 회원의 레벨 정보
     */
    public MemberLevelResponse getMemberLevel(Long memberId) {
        MemberStatistics statistics = memberStatisticsRepository.findByMemberId(memberId)
            .orElseThrow(()-> new NotFoundException("회원 통계 데이터를 찾을 수 없습니다."));

        int exp = statistics.calculateExp();
        int level = statistics.calculateLevel(exp);

        if (level == 0)
            return MemberLevelResponse.of(0, 0, 0);

        int currentExp = statistics.getCurrentExpInLevel(exp, level);
        int nextExp = statistics.getNextExpInLevel(level);

        return MemberLevelResponse.of(level, currentExp, nextExp);
    }
}