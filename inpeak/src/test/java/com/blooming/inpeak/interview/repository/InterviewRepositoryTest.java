package com.blooming.inpeak.interview.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.blooming.inpeak.IntegrationTestSupport;
import com.blooming.inpeak.interview.domain.Interview;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class InterviewRepositoryTest {

    @Autowired
    private InterviewRepository interviewRepository;

    @Test
    @DisplayName("회원의 인터뷰 개수를 조회하면 정확한 개수를 반환해야 한다.")
    void countByMemberId_ShouldReturnCorrectCount() {
        // given
        Long memberId = 1L;
        interviewRepository.save(Interview.of(memberId, LocalDate.now()));
        interviewRepository.save(Interview.of(memberId, LocalDate.now().minusDays(1)));

        // when
        long count = interviewRepository.countByMemberId(memberId);

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("회원이 특정 날짜에 인터뷰를 진행한 경우 true, 진행하지 않은 경우 false를 반환해야 한다.")
    void existsByMemberIdAndStartDate_ShouldReturnTrueIfExists() {
        // given
        Long memberId = 1L;
        LocalDate today = LocalDate.now();
        interviewRepository.save(Interview.of(memberId, today));

        // when
        boolean existsToday = interviewRepository.existsByMemberIdAndStartDate(memberId, today);
        boolean existsYesterday = interviewRepository.existsByMemberIdAndStartDate(memberId, today.minusDays(1));

        // then
        assertThat(existsToday).isTrue();
        assertThat(existsYesterday).isFalse();
    }
}
