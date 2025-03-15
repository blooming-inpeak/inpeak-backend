package com.blooming.inpeak.answer.repository;

import com.blooming.inpeak.answer.domain.Answer;
import com.blooming.inpeak.answer.dto.response.UserStatsResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    /**
     * 특정 멤버가 특정 날짜에 진행한 인터뷰의 답변 리스트 조회
     */
    @Query("SELECT a FROM Answer a " +
        "JOIN FETCH a.interview i " +
        "JOIN FETCH a.question q " +
        "WHERE i.memberId = :memberId " +
        "AND i.startDate = :date")
    List<Answer> findAnswersByMemberAndDate(
        @Param("memberId") Long memberId,
        @Param("date") LocalDate date
    );

    /**
     * 특정 멤버의 활동 내용 통계
     */
    @Query("""
    SELECT new com.blooming.inpeak.answer.dto.response.UserStatsResponse(
        COUNT(a.id),
        SUM(CASE WHEN a.status = 'CORRECT' THEN 1 ELSE 0 END),
        SUM(CASE WHEN a.status = 'INCORRECT' THEN 1 ELSE 0 END),
        SUM(CASE WHEN a.status = 'SKIPPED' THEN 1 ELSE 0 END),
        COUNT(DISTINCT a.interviewId),
        COALESCE(SUM(a.runningTime), 0)
    )
    FROM Answer a
    WHERE a.memberId = :memberId
""")
    UserStatsResponse getUserStats(@Param("memberId") Long memberId);


    /**
     * 특정 답변 ID로 답변 조회 (인터뷰와 질문을 패치 조인하여 조회)
     */
    @Query("SELECT a FROM Answer a " +
        "JOIN FETCH a.interview i " +
        "JOIN FETCH a.question q " +
        "WHERE a.id = :answerId")
    Optional<Answer> findAnswerById(@Param("answerId") Long answerId);

    /**
     * 특정 회원의 답변 성공률을 계산하는 쿼리
     * SKIPPED 상태의 답변은 제외하고 계산
     */
    @Query("""
    SELECT COALESCE(
        CAST(SUM(CASE WHEN a.status = 'CORRECT' THEN 1 ELSE 0 END) * 100 AS INTEGER) / 
        NULLIF(CAST(SUM(CASE WHEN a.status IN ('CORRECT', 'INCORRECT') THEN 1 ELSE 0 END) AS INTEGER), 0),
        0)
    FROM Answer a
    WHERE a.memberId = :memberId
    """)
    int getMemberSuccessRate(@Param("memberId") Long memberId);

    /**
     * 전체 사용자의 평균 답변 성공률을 계산하는 쿼리
     * SKIPPED 상태의 답변은 제외하고 계산
     */
    @Query("""
    SELECT COALESCE(
        CAST(SUM(CASE WHEN a.status = 'CORRECT' THEN 1 ELSE 0 END) * 100 AS INTEGER) / 
        NULLIF(CAST(SUM(CASE WHEN a.status IN ('CORRECT', 'INCORRECT') THEN 1 ELSE 0 END) AS INTEGER), 0),
        0)
    FROM Answer a
    """)
    int getAverageSuccessRate();
}
