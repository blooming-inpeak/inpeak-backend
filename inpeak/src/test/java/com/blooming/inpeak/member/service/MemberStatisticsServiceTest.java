package com.blooming.inpeak.member.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.blooming.inpeak.member.domain.MemberStatistics;
import com.blooming.inpeak.member.dto.response.MemberStatsResponse;
import com.blooming.inpeak.member.dto.response.SuccessRateResponse;
import com.blooming.inpeak.member.repository.MemberStatisticsRepository;
import com.blooming.inpeak.member.dto.response.MemberLevelResponse;
import com.blooming.inpeak.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


public class MemberStatisticsServiceTest extends IntegrationTestSupport {

    @Autowired
    private MemberStatisticsService memberStatisticsService;

    @Autowired
    private MemberStatisticsRepository memberStatisticsRepository;

    private final Long memberId = 1L;
    private final Long otherMemberId = 2L;

    @Test
    @Transactional
    @DisplayName("getMemberLevel()은 경험치가 0일 때 올바른 레벨 정보를 반환해야 한다.")
    void getMemberLevel_ShouldReturnLevelInformation_WhenExpIsZero() {
        // given
        memberStatisticsRepository.save(MemberStatistics.of(memberId));

        // when
        MemberLevelResponse response = memberStatisticsService.getMemberLevel(memberId);

        // then
        assertThat(response.level()).isEqualTo(0);
        assertThat(response.currentExp()).isEqualTo(0);
        assertThat(response.nextExp()).isEqualTo(0);
    }

    @Test
    @Transactional
    @DisplayName("getMemberLevel()은 경험치 40일 때 올바른 레벨 정보를 반환해야 한다.")
    void getMemberLevel_ShouldReturnCorrectLevelInformation_WhenExpIs40() {
        // given
        MemberStatistics stat = memberStatisticsRepository.save(MemberStatistics.of(memberId));

        stat.increaseCorrect();
        stat.increaseCorrect();
        stat.increaseCorrect();
        stat.increaseCorrect();

        // when
        MemberLevelResponse response = memberStatisticsService.getMemberLevel(memberId);

        // then
        assertThat(response.level()).isEqualTo(2);
        assertThat(response.currentExp()).isEqualTo(10);
        assertThat(response.nextExp()).isEqualTo(60);
    }

    @Test
    @Transactional
    @DisplayName("getMemberLevel()은 경험치가 1350을 초과하면 최대 레벨과 올바른 경험치 정보를 반환해야 한다.")
    void getMemberLevel_ShouldReturnMaxLevel_WhenExpExceeds1350() {
        // given
        MemberStatistics stat = memberStatisticsRepository.save(MemberStatistics.of(memberId));

        for (int i = 0; i < 135; i++) {
            stat.increaseCorrect();
        }
        stat.increaseIncorrect();

        // when
        MemberLevelResponse response = memberStatisticsService.getMemberLevel(memberId);

        // then
        assertThat(response.level()).isEqualTo(10);
        assertThat(response.currentExp()).isEqualTo(5);
        assertThat(response.nextExp()).isEqualTo(0);
    }

    @Test
    @Transactional
    @DisplayName("getSuccessRate()은 특정 회원 및 전체 통계가 없는 경우, 성공률은 모두 0이어야 한다.")
    void getSuccessRate_ShouldReturnZero_IfNoStatisticsExist() {
        // given
        memberStatisticsRepository.save(MemberStatistics.of(memberId));

        // when
        SuccessRateResponse response = memberStatisticsService.getSuccessRate(memberId);

        // then
        assertThat(response.userSuccessRate()).isEqualTo(0);
        assertThat(response.averageSuccessRate()).isEqualTo(0);
    }

    @Test
    @Transactional
    @DisplayName("getSuccessRate()은 전체 통계는 있지만 특정 회원 통계가 없는 경우, 회원 성공률만 0이어야 한다.")
    void getSuccessRate_ShouldReturnZero_IfNoMemberStatistics() {
        // given
        memberStatisticsRepository.save(MemberStatistics.of(memberId));
        MemberStatistics otherStat = memberStatisticsRepository.save(MemberStatistics.of(otherMemberId));

        otherStat.increaseCorrect();
        otherStat.increaseIncorrect();
        otherStat.increaseSkipped();

        // when
        SuccessRateResponse response = memberStatisticsService.getSuccessRate(memberId);

        // then
        assertThat(response.userSuccessRate()).isEqualTo(0);
        assertThat(response.averageSuccessRate()).isEqualTo(33);
    }

    @Test
    @Transactional
    @DisplayName("getSuccessRate()은 특정 회원 통계만 존재하는 경우, 특정 회원 및 전체 성공률은 동일해야 한다.")
    void getSuccessRate_ShouldReturnSameRates_IfOnlyOneMemberExists() {
        // given
        MemberStatistics stat = memberStatisticsRepository.save(MemberStatistics.of(memberId));

        stat.increaseCorrect();
        stat.increaseSkipped();

        // when
        SuccessRateResponse response = memberStatisticsService.getSuccessRate(memberId);

        // then
        assertThat(response.userSuccessRate()).isEqualTo(50);
        assertThat(response.averageSuccessRate()).isEqualTo(response.userSuccessRate());
    }

    @Test
    @Transactional
    @DisplayName("getSuccessRate()은 특정 회원과 다른 회원들의 통계가 존재하는 경우, 각 성공률은 정확히 계산되어야 한다.")
    void getSuccessRate_ShouldReturnCorrectRates_IfMultipleUsersExist() {
        // given
        MemberStatistics stat = memberStatisticsRepository.save(MemberStatistics.of(memberId));
        MemberStatistics otherStat = memberStatisticsRepository.save(MemberStatistics.of(otherMemberId));

        // 해당 회원 성공률 33
        stat.increaseCorrect();
        stat.increaseIncorrect();
        stat.increaseSkipped();

        // 전체 회원 성공률 50
        otherStat.increaseCorrect();

        // when
        SuccessRateResponse response = memberStatisticsService.getSuccessRate(memberId);

        // then
        assertThat(response.userSuccessRate()).isEqualTo(33);
        assertThat(response.averageSuccessRate()).isEqualTo(50);
    }

    @Test
    @Transactional
    @DisplayName("getMemberStats()는 특정 회원의 통계가 없는 경우 모두 0을 반환해야 한다.")
    void getMemberStats_ShouldReturnZero_IfNoMemberStatistics() {
        //given
        memberStatisticsRepository.save(MemberStatistics.of(memberId));

        //when
        MemberStatsResponse response = memberStatisticsService.getMemberStats(memberId);

        //then
        assertThat(response.totalAnswerCount()).isEqualTo(0);
        assertThat(response.correctAnswerCount()).isEqualTo(0);
        assertThat(response.incorrectAnswerCount()).isEqualTo(0);
        assertThat(response.skippedAnswerCount()).isEqualTo(0);
    }

    @Test
    @Transactional
    @DisplayName("getMemberStats()는 특정 회원의 정확한 통계 정보를 반환해야 한다.")
    void getMemberStats_ShouldReturnCorrectStats_IfMemberStatsExist() {
        //given
        MemberStatistics stat = memberStatisticsRepository.save(MemberStatistics.of(memberId));

        stat.increaseCorrect();
        stat.increaseCorrect();
        stat.increaseCorrect();
        stat.increaseIncorrect();
        stat.increaseIncorrect();
        stat.increaseSkipped();

        //when
        MemberStatsResponse response = memberStatisticsService.getMemberStats(memberId);

        //then
        assertThat(response.totalAnswerCount()).isEqualTo(6);
        assertThat(response.correctAnswerCount()).isEqualTo(3);
        assertThat(response.incorrectAnswerCount()).isEqualTo(2);
        assertThat(response.skippedAnswerCount()).isEqualTo(1);
    }
}
