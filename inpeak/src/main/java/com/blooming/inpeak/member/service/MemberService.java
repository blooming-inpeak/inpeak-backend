package com.blooming.inpeak.member.service;

import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.member.domain.Member;
import com.blooming.inpeak.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    private static final int[] LEVEL_EXP_TABLE = {0, 30, 90, 180, 300, 450, 630, 840, 1080, 1350};

    /**
     * 회원에게 경험치 부여 및 레벨업 시키는 메서드
     *
     * @param memberId 사용자 ID
     * @param status   답변 상태
     */
    @Transactional
    public void awardExperience(Long memberId, AnswerStatus status) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        // 경험치 부여 및 레벨업
        member.increaseExperience(status.getExperiencePoints());
        if (checkLevelUp(member.getExperience(), member.getLevel())) {
            member.increaseLevel();
        }

        memberRepository.save(member);
    }

    /**
     * 테스트에서 사용할 레벨 경험치 테이블 반환
     */
    public int[] getLevelExpTable() {
        return LEVEL_EXP_TABLE.clone();
    }

    private boolean checkLevelUp(int experience, int currentLevel) {
        if (currentLevel >= LEVEL_EXP_TABLE.length - 1) {
            return false; // 최대 레벨이면 레벨업 불가
        }
        return experience >= LEVEL_EXP_TABLE[currentLevel];
    }
}
