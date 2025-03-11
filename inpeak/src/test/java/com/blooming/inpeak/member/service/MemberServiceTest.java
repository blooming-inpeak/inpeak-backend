package com.blooming.inpeak.member.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.member.domain.Member;
import com.blooming.inpeak.member.domain.OAuth2Provider;
import com.blooming.inpeak.member.domain.RegistrationStatus;
import com.blooming.inpeak.member.dto.response.MemberLevelResponse;
import com.blooming.inpeak.member.repository.MemberRepository;
import com.blooming.inpeak.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class MemberServiceTest extends IntegrationTestSupport {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @Transactional
    @DisplayName("CORRECT 답변 시 경험치를 10 증가시켜야 한다.")
    void awardExperience_ShouldIncreaseExperience_WhenCorrectAnswer() {
        // given
        Long memberId = memberRepository.save(
            Member.of("test@example.com", "nickname", "token",
                OAuth2Provider.KAKAO, RegistrationStatus.COMPLETED)).getId();

        // when
        memberService.awardExperience(memberId, AnswerStatus.CORRECT);

        // then
        Member updatedMember = memberRepository.findById(memberId).orElseThrow();
        assertThat(updatedMember.getExperience()).isEqualTo(10);
        assertThat(updatedMember.getLevel()).isEqualTo(1);
    }

    @Test
    @Transactional
    @DisplayName("INCORRECT 답변 시 경험치를 5 증가시켜야 한다.")
    void awardExperience_ShouldIncreaseExperience_WhenIncorrectAnswer() {
        // given
        Long memberId = memberRepository.save(
            Member.of("test@example.com", "nickname", "token",
                OAuth2Provider.KAKAO, RegistrationStatus.COMPLETED)).getId();

        // when
        memberService.awardExperience(memberId, AnswerStatus.INCORRECT);

        // then
        Member updatedMember = memberRepository.findById(memberId).orElseThrow();
        assertThat(updatedMember.getExperience()).isEqualTo(5);
        assertThat(updatedMember.getLevel()).isEqualTo(1);
    }

    @Test
    @Transactional
    @DisplayName("SKIPPED 답변 시 경험치를 증가시키지 않아야 한다.")
    void awardExperience_ShouldNotIncreaseExperience_WhenSkippedAnswer() {
        // given
        Long memberId = memberRepository.save(
            Member.of("test@example.com", "nickname", "token",
                OAuth2Provider.KAKAO, RegistrationStatus.COMPLETED)).getId();

        // when
        memberService.awardExperience(memberId, AnswerStatus.SKIPPED);

        // then
        Member updatedMember = memberRepository.findById(memberId).orElseThrow();
        assertThat(updatedMember.getExperience()).isEqualTo(0);
        assertThat(updatedMember.getLevel()).isEqualTo(1);
    }

    @Test
    @Transactional
    @DisplayName("경험치가 레벨업 기준을 넘으면 레벨을 증가시켜야 한다.")
    void awardExperience_ShouldIncreaseLevel_WhenExperienceMeetsThreshold() {
        // given
        Long memberId = memberRepository.save(
            Member.of("test@example.com", "nickname", "token",
                OAuth2Provider.KAKAO, RegistrationStatus.COMPLETED)).getId();

        // given
        memberService.awardExperience(memberId, AnswerStatus.CORRECT);
        memberService.awardExperience(memberId, AnswerStatus.CORRECT);
        memberService.awardExperience(memberId, AnswerStatus.CORRECT);

        // when
        Member updatedMember = memberRepository.findById(memberId).orElseThrow();

        // then
        assertThat(updatedMember.getExperience()).isEqualTo(30);
        assertThat(updatedMember.getLevel()).isEqualTo(2);
    }

    @Test
    @Transactional
    @DisplayName("최대 레벨에서는 레벨이 증가하지 않아야 한다.")
    void awardExperience_ShouldNotIncreaseLevel_WhenAlreadyAtMaxLevel() {
        // given
        Member maxLevelMember = memberRepository.save(
            Member.of("max@example.com", "maxUser", "token",
                OAuth2Provider.KAKAO, RegistrationStatus.COMPLETED)
        );

        int[] levelExpTable = memberService.getLevelExpTable();
        for (int i = 1; i < levelExpTable.length; i++) {
            maxLevelMember.increaseLevel();
        }
        maxLevelMember.increaseExperience(levelExpTable[levelExpTable.length - 1]);
        memberRepository.save(maxLevelMember);

        int beforeLevel = maxLevelMember.getLevel();
        int beforeExperience = maxLevelMember.getExperience();

        // when
        memberService.awardExperience(maxLevelMember.getId(), AnswerStatus.CORRECT);

        // then
        Member updatedMember = memberRepository.findById(maxLevelMember.getId()).orElseThrow();

        assertThat(updatedMember.getLevel()).isEqualTo(levelExpTable.length);
        assertThat(updatedMember.getLevel()).isEqualTo(beforeLevel); // 레벨이 증가하지 않아야 함
        assertThat(updatedMember.getExperience()).isGreaterThan(beforeExperience); // 경험치는 증가
    }

    @Test
    @Transactional
    @DisplayName("회원의 레벨 정보를 정상적으로 반환해야 한다.")
    void getMemberLevel_ShouldReturnCorrectLevelInfo() {
        // given
        Member member = memberRepository.save(
            Member.of("test@example.com", "nickname", "token",
                OAuth2Provider.KAKAO, RegistrationStatus.COMPLETED));

        int initialExp = 45;
        member.increaseExperience(initialExp);
        member.increaseLevel();
        memberRepository.save(member);

        // when
        MemberLevelResponse response = memberService.getMemberLevel(member.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.level()).isEqualTo(2);
        assertThat(response.currentExp()).isEqualTo(15);
        assertThat(response.nextExp()).isEqualTo(60);
    }

    @Test
    @Transactional
    @DisplayName("최대 레벨에서의 정보를 정상적으로 반환해야 한다.")
    void getMemberLevel_ShouldReturnCorrectMaxLevelInfo() {
        // given
        Member maxLevelMember = memberRepository.save(
            Member.of("max@example.com", "maxUser", "token",
                OAuth2Provider.KAKAO, RegistrationStatus.COMPLETED)
        );

        int[] levelExpTable = memberService.getLevelExpTable();
        for (int i = 1; i < levelExpTable.length; i++) {
            maxLevelMember.increaseLevel();
        }
        maxLevelMember.increaseExperience(levelExpTable[levelExpTable.length - 1] + 15);
        memberRepository.save(maxLevelMember);

        // when
        MemberLevelResponse response = memberService.getMemberLevel(maxLevelMember.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.level()).isEqualTo(levelExpTable.length);
        assertThat(response.currentExp()).isEqualTo(285);
        assertThat(response.nextExp()).isEqualTo(270);
    }
}
