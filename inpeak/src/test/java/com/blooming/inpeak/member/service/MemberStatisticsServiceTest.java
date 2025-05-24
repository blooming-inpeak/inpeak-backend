package com.blooming.inpeak.member.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.blooming.inpeak.member.domain.MemberStatistics;
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

    @DisplayName("getMemberLevel()은 경험치가 0일 때 올바른 레벨 정보를 반환해야 한다.")
    @Transactional
    @Test
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

    @DisplayName("getMemberLevel()은 경험치 40일 때 올바른 레벨 정보를 반환해야 한다.")
    @Transactional
    @Test
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

    @DisplayName("getMemberLevel()은 경험치가 1350을 초과하면 최대 레벨과 올바른 경험치 정보를 반환해야 한다.")
    @Transactional
    @Test
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
}
