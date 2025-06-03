package com.blooming.inpeak.common.utils;

import com.blooming.inpeak.member.repository.MemberRepository;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NicknameGenerator {

    private static final List<String> ADJECTIVES = Arrays.asList(
        "행복한", "즐거운", "신나는", "활기찬", "용감한", "재밌는", "영리한",
        "친절한", "당당한", "귀여운", "멋진", "똑똑한", "힘찬", "빛나는",
        "우아한", "씩씩한", "상냥한", "밝은", "포근한", "따뜻한", "시원한",
        "달콤한", "깔끔한", "유쾌한", "춤추는", "꿈꾸는", "활발한", "멋진"
    );

    private static final List<String> ANIMALS = Arrays.asList(
        // 1글자 동물
        "곰", "말", "양", "소", "돼지", "닭", "개", "고양이",

        // 2글자 동물
        "사자", "여우", "토끼", "늑대", "사슴", "오리", "표범", "악어",
        "하마", "참새", "까치", "나비", "벌", "개미", "거미", "뱀",
        "쥐", "다람쥐", "햄스터", "물개", "바다사자", "치타", "수달", "너구리",
        "족제비", "멧돼지", "고라니", "산양", "염소", "당나귀", "얼룩말",
        "하이에나", "코뿔소", "미어캣", "원숭이", "침팬지", "고릴라",

        // 3글자 동물
        "코끼리", "팬더", "고래", "독수리", "기린", "펭귄", "돌고래",
        "부엉이", "캥거루", "코알라", "거북이", "물고기", "북극곰",
        "판다곰", "강아지", "고양이", "앵무새", "두더지", "청설모"
    );

    private static final int MAX_ATTEMPTS = 30;
    private static final int MAX_FALLBACK_ATTEMPTS = 50;
    private static final int NUMBER_RANGE = 9;
    private static final int NUMBER_START = 1;

    private final MemberRepository memberRepository;
    private final Random random = new SecureRandom();

    public String generateUniqueNickname() {
        try {
            return generateUniqueNicknameWithRetry(0);
        } catch (IllegalStateException e) {
            log.warn("동물 닉네임 생성 실패, 대체 닉네임 생성: {}", e.getMessage());
            return generateFallbackNickname();
        }
    }

    private String generateUniqueNicknameWithRetry(int attempts) {
        if (attempts >= MAX_ATTEMPTS) {
            throw new IllegalStateException("동물 닉네임 생성 실패: 최대 시도 횟수 초과");
        }

        String nickname = generateNickname();
        if (memberRepository.existsByNickname(nickname)) {
            log.debug("닉네임 중복 발생 ({}회 시도): {}", attempts + 1, nickname);
            return generateUniqueNicknameWithRetry(attempts + 1);
        }

        return nickname;
    }

    private String generateFallbackNickname() {
        for (int i = 0; i < MAX_FALLBACK_ATTEMPTS; i++) {
            String fallback = String.format("회원%04d",
                random.nextInt(10000)); // "회원0001" ~ "회원9999" (6글자)

            if (!memberRepository.existsByNickname(fallback)) {
                return fallback;
            }
        }

        // 최후의 수단: UUID 기반 (7글자)
        String uuid = UUID.randomUUID().toString().substring(0, 6);
        return String.format("u%s", uuid); // "uabc123"
    }

    String generateNickname() {
        String adjective = ADJECTIVES.get(random.nextInt(ADJECTIVES.size()));
        String animal = ANIMALS.get(random.nextInt(ANIMALS.size()));
        int number = random.nextInt(NUMBER_RANGE) + NUMBER_START;

        String nickname = String.format("%s %s%d", adjective, animal, number);
        if (nickname.length() > 8) {
            return generateNickname();
        }

        return nickname;
    }
}
