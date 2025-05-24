package com.blooming.inpeak.answer.repository;

import com.blooming.inpeak.answer.domain.Answer;
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
     * 특정 회원의 총 인터뷰 개수
     */
    @Query("""
        SELECT COUNT(DISTINCT a.interviewId)
        FROM Answer a
        WHERE a.memberId = :memberId
    """)
    long countTotalInterviewsByMemberId(@Param("memberId") Long memberId);

    /**
     * 특정 회원의 총 답변 시간
     */
    @Query("""
        SELECT COALESCE(SUM(a.runningTime), 0)
        FROM Answer a
        WHERE a.memberId = :memberId
    """)
    long sumTotalRunningTimeByMemberId(@Param("memberId") Long memberId);

    void deleteByMemberId(Long id);

    /**
     * 특정 멤버의 모든 비디오 URL 조회
     */
    @Query(value = "SELECT video_url FROM answers WHERE member_id = :memberId", nativeQuery = true)
    List<String> findAllVideoUrlsByMemberId(@Param("memberId") Long memberId);

    Optional<Answer> findByInterviewIdAndQuestionId(Long interviewId, Long questionId);

    boolean existsByInterviewIdAndQuestionId(Long interviewId, Long questionId);
}
