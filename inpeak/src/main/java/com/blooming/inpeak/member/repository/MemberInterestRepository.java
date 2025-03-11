package com.blooming.inpeak.member.repository;

import com.blooming.inpeak.member.domain.InterestType;
import com.blooming.inpeak.member.domain.MemberInterest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberInterestRepository extends JpaRepository<MemberInterest, Long> {

    @Query("SELECT mi.interestType FROM MemberInterest mi WHERE mi.memberId = :memberId")
    List<InterestType> findInterestsByMemberId(Long memberId);

    void deleteByMemberId(Long id);
}
