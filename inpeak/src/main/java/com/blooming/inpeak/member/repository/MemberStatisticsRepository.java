package com.blooming.inpeak.member.repository;

import com.blooming.inpeak.member.domain.MemberStatistics;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberStatisticsRepository extends JpaRepository<MemberStatistics, Long> {

    Optional<MemberStatistics> findByMemberId(Long memberId);

    /**
     * 특정 회원의 답변 성공률 (skipped 포함)
     */
    @Query("""
        SELECT COALESCE(
            ms.correctCount * 100 /
            NULLIF(ms.correctCount + ms.incorrectCount + ms.skippedCount, 0),
            0)
        FROM MemberStatistics ms
        WHERE ms.memberId = :memberId
    """)
    int getMemberSuccessRate(@Param("memberId") Long memberId);

    /**
     * 전체 평균 답변 성공률 (skipped 포함)
     */
    @Query("""
        SELECT COALESCE(
            SUM(ms.correctCount) * 100 /
            NULLIF(SUM(ms.correctCount + ms.incorrectCount + ms.skippedCount), 0),
            0)
        FROM MemberStatistics ms
    """)
    int getAverageSuccessRate();
}