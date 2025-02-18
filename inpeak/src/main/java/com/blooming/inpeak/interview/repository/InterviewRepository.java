package com.blooming.inpeak.interview.repository;

import com.blooming.inpeak.interview.domain.Interview;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    long countByMemberId(Long memberId);

    boolean existsByMemberIdAndStartDate(Long memberId, LocalDate startDate);
}
