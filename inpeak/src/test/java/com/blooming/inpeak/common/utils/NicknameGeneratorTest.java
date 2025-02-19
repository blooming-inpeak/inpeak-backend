package com.blooming.inpeak.common.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.blooming.inpeak.member.repository.MemberRepository;
import com.blooming.inpeak.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("닉네임 생성기 테스트")
class NicknameGeneratorTest extends IntegrationTestSupport {

    @Autowired
    private NicknameGenerator nicknameGenerator;

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("고유 닉네임 생성 성공")
    @Test
    void generateUniqueNickname() {
        // when
        String nickname = nicknameGenerator.generateUniqueNickname();

        // then
        assertThat(nickname).isNotNull();
        assertThat(nickname).matches("^.+ .+ .+ \\d{4}$"); // 단어들과 숫자 4자리 패턴
        assertThat(memberRepository.existsByNickname(nickname)).isFalse();
    }
}
