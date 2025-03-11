package com.blooming.inpeak.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.member.domain.Member;
import com.blooming.inpeak.member.domain.OAuth2Provider;
import com.blooming.inpeak.member.domain.RegistrationStatus;
import com.blooming.inpeak.member.repository.MemberRepository;
import com.blooming.inpeak.support.IntegrationTestSupport;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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
    @DisplayName("awardExperience()는 CORRECT 답변 시 경험치를 10 증가시켜야 한다.")
    void awardExperience_ShouldIncreaseExperience_WhenCorrectAnswer() {
        // given
        Long memberId = memberRepository.save(
            Member.of("test@example.com", "nickname", UUID.randomUUID().toString(),
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
    @DisplayName("awardExperience()는 INCORRECT 답변 시 경험치를 5 증가시켜야 한다.")
    void awardExperience_ShouldIncreaseExperience_WhenIncorrectAnswer() {
        // given
        Long memberId = memberRepository.save(
            Member.of("test@example.com", "nickname", UUID.randomUUID().toString(),
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
    @DisplayName("awardExperience()는 SKIPPED 답변 시 경험치를 증가시키지 않아야 한다.")
    void awardExperience_ShouldNotIncreaseExperience_WhenSkippedAnswer() {
        // given
        Long memberId = memberRepository.save(
            Member.of("test@example.com", "nickname", UUID.randomUUID().toString(),
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
    @DisplayName("awardExperience()는 경험치가 레벨업 기준을 넘으면 레벨을 증가시켜야 한다.")
    void awardExperience_ShouldIncreaseLevel_WhenExperienceMeetsThreshold() {
        // given
        Long memberId = memberRepository.save(
            Member.of("test@example.com", "nickname", UUID.randomUUID().toString(),
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
    @DisplayName("awardExperience()는 최대 레벨에서는 레벨이 증가하지 않아야 한다.")
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

        assertThat(updatedMember.getLevel()).isEqualTo(beforeLevel); // 레벨이 증가하지 않아야 함
        assertThat(updatedMember.getExperience()).isGreaterThan(beforeExperience); // 경험치는 증가
    }

    @Test
    @DisplayName("awardExperience()는 존재하지 않는 회원 ID를 입력하면 예외를 발생시켜야 한다.")
    void awardExperience_ShouldThrowException_WhenMemberNotFound() {
        // when & then
        assertThatThrownBy(() -> memberService.awardExperience(999L, AnswerStatus.CORRECT))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("회원 정보를 찾을 수 없습니다.");
    }
}
