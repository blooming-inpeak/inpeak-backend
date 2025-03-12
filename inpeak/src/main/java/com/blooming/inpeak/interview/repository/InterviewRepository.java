package com.blooming.inpeak.interview.repository;

import com.blooming.inpeak.interview.domain.Interview;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    // 특정 회원 ID와 특정 날짜에 해당하는 인터뷰가 존재하는지 확인
    boolean existsByMemberIdAndStartDate(Long memberId, LocalDate startDate);

    // 특정 멤버 ID와 특정 월에 해당하는 인터뷰 조회
    List<Interview> findByMemberIdAndStartDateBetween(Long memberId, LocalDate start, LocalDate end);
}
