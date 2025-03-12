package com.blooming.inpeak.interview.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.blooming.inpeak.interview.domain.Interview;
import com.blooming.inpeak.support.IntegrationTestSupport;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("InterviewRepository 테스트")
class InterviewRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private InterviewRepository interviewRepository;

    @Test
    @Transactional
    @DisplayName("회원이 특정 날짜에 인터뷰를 진행한 경우 true, 진행하지 않은 경우 false를 반환해야 한다.")
    void existsByMemberIdAndStartDate_ShouldReturnTrueIfExists() {
        // given
        Long memberId = 1L;
        LocalDate today = LocalDate.now();
        interviewRepository.save(Interview.of(memberId, today));

        // when
        boolean existsToday = interviewRepository.existsByMemberIdAndStartDate(memberId, today);
        boolean existsYesterday = interviewRepository.existsByMemberIdAndStartDate(memberId,
            today.minusDays(1));

        // then
        assertThat(existsToday).isTrue();
        assertThat(existsYesterday).isFalse();
    }
}
