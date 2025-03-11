package com.blooming.inpeak.member.domian;

import static org.assertj.core.api.Assertions.assertThat;

import com.blooming.inpeak.member.domain.Member;
import com.blooming.inpeak.member.domain.OAuth2Provider;
import com.blooming.inpeak.member.domain.RegistrationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Member domain 테스트")
class MemberTest {

    @Test
    @DisplayName("increaseExperience()는 경험치를 정상적으로 증가시켜야 한다.")
    void increaseExperience_ShouldIncreaseExperienceCorrectly() {
        // given
        Member member = Member.of("test@example.com", "nickname", "token",
            OAuth2Provider.KAKAO, RegistrationStatus.COMPLETED);

        // when
        member.increaseExperience(10);

        // then
        assertThat(member.getExperience()).isEqualTo(10);
    }

    @Test
    @DisplayName("increaseExperience()는 경험치를 누적 증가시켜야 한다.")
    void increaseExperience_ShouldAccumulateExperienceCorrectly() {        // given
        Member member = Member.of("test@example.com", "nickname", "token",
            OAuth2Provider.KAKAO, RegistrationStatus.COMPLETED);

        // when
        member.increaseExperience(10);
        member.increaseExperience(20);

        // then
        assertThat(member.getExperience()).isEqualTo(30);
    }

    @Test
    @DisplayName("increaseLevel()은 레벨을 1 증가시켜야 한다.")
    void increaseLevel_ShouldIncreaseLevelByOne() {        // given
        Member member = Member.of("test@example.com", "nickname", "token",
            OAuth2Provider.KAKAO, RegistrationStatus.COMPLETED);

        // when
        member.increaseLevel();

        // then
        assertThat(member.getLevel()).isEqualTo(2);
    }
}