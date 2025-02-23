package com.blooming.inpeak.member.repository;

import com.blooming.inpeak.member.domain.Interests;
import com.blooming.inpeak.member.domain.MemberInterests;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberInterestsRepository extends JpaRepository<MemberInterests, Long> {

    @Query("SELECT mi.interest FROM MemberInterests mi WHERE mi.memberId = :memberId")
    List<Interests> findInterestsByMemberId(Long memberId);
}
