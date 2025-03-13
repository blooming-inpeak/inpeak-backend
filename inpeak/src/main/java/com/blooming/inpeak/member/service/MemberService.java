package com.blooming.inpeak.member.service;

import com.blooming.inpeak.member.domain.Member;
import com.blooming.inpeak.member.dto.request.NincknameUpdateRequest;
import com.blooming.inpeak.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public String updateNickName(Long memberId, String nickName) {

        if (memberRepository.existsByNickname(nickName)) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다: " + nickName);
        }

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + memberId));
        member.updateNickname(nickName);

        return nickName;
    }
}
