package com.blooming.inpeak.member.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.blooming.inpeak.member.domain.Member;
import com.blooming.inpeak.member.domain.OAuth2Provider;
import com.blooming.inpeak.support.IntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("MemberRepository 테스트")
class MemberRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private MemberRepository memberRepository;

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
    }

    // repository < (service test, 더 상위 개념)

    @DisplayName("이메일로 회원 조회")
    @Test
    @Transactional
    void findByEmail() {
        // given
        Long kakaoId = 1234567890L;
        String testEmail = "test@test.com";
        Member member = Member.builder()
            .kakaoId(kakaoId)
            .nickname("테스트유저")
            .provider(OAuth2Provider.KAKAO)
            .totalQuestionCount(0L)
            .correctAnswerCount(0L)
            .kakaoEmail(testEmail)
            .build();
        memberRepository.save(member);

        // when
        Member found = memberRepository.findByKakaoId(kakaoId)
            .orElseThrow(() -> new IllegalStateException("해당 이메일의 회원이 존재하지 않습니다."));

        // then
        assertThat(found.getKakaoId()).isEqualTo(kakaoId);
    }

    @DisplayName("닉네임으로 회원 조회")
    @Test
    @Transactional
    void existsByNickname() {
        //given
        Long kakaoId = 1234567890L;
        String nickname = "유니크 닉네임";
        String testEmail = "test@test.com";

        Member member = Member.builder()
            .kakaoId(kakaoId)
            .nickname(nickname)
            .provider(OAuth2Provider.KAKAO)
            .totalQuestionCount(0L)
            .correctAnswerCount(0L)
            .kakaoEmail(testEmail)
            .build();
        memberRepository.save(member);

        // when & then
        assertThat(memberRepository.existsByNickname(nickname)).isTrue();
        assertThat(memberRepository.existsByNickname("존재하지않는닉네임")).isFalse();
    }

}
