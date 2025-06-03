package com.blooming.inpeak.common.utils;

import com.blooming.inpeak.member.repository.MemberRepository;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NicknameGenerator {

    private static final List<String> ADJECTIVES = Arrays.asList(
        "행복한", "즐거운", "신나는", "활기찬", "용감한", "지혜로운", "영리한",
        "친절한", "당당한", "귀여운", "멋진", "싱그러운", "힘찬", "열정적인"
        // ... 더 많은 형용사 추가
    );

    private static final List<String> ACTIONS = Arrays.asList(
        "뛰어가는", "춤추는", "노래하는", "웃고있는", "달리는", "산책하는",
        "공부하는", "여행하는", "탐험하는", "생각하는", "그리는", "읽고있는"
        // ... 더 많은 동작 추가
    );

    private static final List<String> ANIMALS = Arrays.asList(
        "코끼리", "사자", "호랑이", "팬더", "고래", "독수리", "늑대", "여우",
        "토끼", "기린", "곰", "펭귄", "돌고래", "부엉이", "캥거루", "고양이"
        // ... 더 많은 동물 추가
    );

    private final MemberRepository memberRepository;
    private final Random random = new SecureRandom();

    private static final int NUMBER_RANGE = 9000;
    private static final int NUMBER_START = 1000;

    public String generateUniqueNickname() {
        String nickname = generateNickname();

        boolean result = memberRepository.existsByNickname(nickname);

        if (result) {
            log.info("닉네임 중복 발생. 다시 생성합니다.");
            return generateUniqueNickname();
        }

        return nickname;
    }

    private String generateNickname() {
        String action = ACTIONS.get(random.nextInt(ACTIONS.size()));
        String adjective = ADJECTIVES.get(random.nextInt(ADJECTIVES.size()));
        String animal = ANIMALS.get(random.nextInt(ANIMALS.size()));
        int number = random.nextInt(NUMBER_RANGE) + NUMBER_START;

        return String.format("%s %s %s %d", action, adjective, animal, number);
    }
}
