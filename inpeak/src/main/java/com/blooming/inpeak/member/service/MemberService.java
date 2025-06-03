package com.blooming.inpeak.member.service;

import com.blooming.inpeak.answer.repository.AnswerRepository;
import com.blooming.inpeak.answer.service.AnswerVideoCleanupService;
import com.blooming.inpeak.auth.repository.RefreshTokenRepository;
import com.blooming.inpeak.common.error.exception.NotFoundException;
import com.blooming.inpeak.interview.repository.InterviewRepository;
import com.blooming.inpeak.member.domain.Member;
import com.blooming.inpeak.member.repository.MemberInterestRepository;
import com.blooming.inpeak.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final AnswerRepository answerRepository;
    private final InterviewRepository interviewRepository;
    private final MemberInterestRepository memberInterestRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AnswerVideoCleanupService answerVideoCleanupService;

    @Transactional
    public String updateNickName(Long memberId, String nickName) {

        if (memberRepository.existsByNickname(nickName)) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다: " + nickName);
        }

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다: " + memberId));
        member.updateNickname(nickName);

        return nickName;
    }

    @Transactional
    public void withdrawMember(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(
            () -> new NotFoundException("존재하지 않는 회원입니다: " + id));

        answerRepository.deleteByMemberId(id);
        interviewRepository.deleteByMemberId(id);
        memberInterestRepository.deleteByMemberId(id);
        refreshTokenRepository.deleteByMemberId(id);
        memberRepository.delete(member);
        answerVideoCleanupService.deleteAllS3Objects(id);
    }

    public Member getMemberInfo(Long id) {
        return memberRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다: " + id));
    }
}
