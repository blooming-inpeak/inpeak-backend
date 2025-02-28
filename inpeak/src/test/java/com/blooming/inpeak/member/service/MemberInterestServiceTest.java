package com.blooming.inpeak.member.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.blooming.inpeak.member.domain.InterestType;
import com.blooming.inpeak.member.domain.MemberInterest;
import com.blooming.inpeak.member.repository.MemberInterestRepository;
import com.blooming.inpeak.support.IntegrationTestSupport;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MemberInterestServiceTest extends IntegrationTestSupport {

    @Autowired
    private MemberInterestService memberInterestService;

    @Autowired
    private MemberInterestRepository memberInterestRepository;

    private static final Long MEMBER_ID = 1L;

    @Test
    @DisplayName("회원 ID로 관심사를 조회하면, 해당 회원의 모든 InterestType을 반환해야 한다.")
    void getUserInterestTypes_ShouldReturnAllInterestsForGivenMemberId() {
        // given
        memberInterestRepository.save(MemberInterest.of(MEMBER_ID, InterestType.REACT));
        memberInterestRepository.save(MemberInterest.of(MEMBER_ID, InterestType.SPRING));

        // when
        List<InterestType> interests = memberInterestService.getUserInterestTypes(MEMBER_ID);

        // then
        assertThat(interests).hasSize(2);
        assertThat(interests).containsExactlyInAnyOrder(InterestType.REACT, InterestType.SPRING);
    }
}
