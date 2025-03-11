package com.blooming.inpeak.member.service;

import com.blooming.inpeak.member.domain.InterestType;
import com.blooming.inpeak.member.domain.Member;
import com.blooming.inpeak.member.domain.MemberInterest;
import com.blooming.inpeak.member.dto.request.MemberInterestRequest;
import com.blooming.inpeak.member.dto.response.MemberInterestResponse;
import com.blooming.inpeak.member.repository.MemberInterestRepository;
import com.blooming.inpeak.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberInterestService {

    private final MemberInterestRepository memberInterestRepository;
    private final MemberRepository memberRepository;

    /**
     * 회원의 관심사를 가져오는 메서드
     *
     * @param memberId 사용자 ID
     * @return 회원의 관심사 리스트
     */
    public List<InterestType> getMemberInterestTypes(Long memberId) {
        return memberInterestRepository.findInterestsByMemberId(memberId);
    }

    /**
     * 회원의 관심사를 포맷팅된 문자열로 가져오는 메서드
     *
     * @param memberId 사용자 ID
     * @return 회원의 관심사 문자열 리스트
     */
    public MemberInterestResponse getMemberInterestStrings(Long memberId) {
        List<InterestType> interests = getMemberInterestTypes(memberId);

        List<String> interestStrings = interests.stream()
            .map(InterestType::toFormattedString)
            .toList();

        return MemberInterestResponse.of(interestStrings);
    }

    @Transactional
    public void registerInitialInterests(Long memberId, MemberInterestRequest request) {
        Member member = getMemberById(memberId);

        if (member.registrationCompleted()) {
            throw new IllegalStateException("이미 회원 등록이 완료되었습니다: " + memberId);
        }

        // 관심사 업데이트
        updateMemberInterests(memberId, request.interestTypes());

        // 초기 등록시에만 회원 상태 업데이트
        member.completeRegistration();
        memberRepository.save(member);
    }

    @Transactional
    public void updateInterests(Long memberId, MemberInterestRequest request) {
        Member member = getMemberById(memberId);

        if (!member.registrationCompleted()) {
            throw new IllegalStateException("회원 등록이 완료되지 않았습니다: " + memberId);
        }

        updateMemberInterests(memberId, request.interestTypes());
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new EntityNotFoundException("해당 사용자가 존재하지 않습니다: " + memberId));
    }

    // TODO: 현재 기존 관심사 delete 후 insert 발생, (성능 고려 해야되나?)
    private void updateMemberInterests(Long memberId, List<InterestType> interestTypes) {
        // 기존 관심사 삭제
        memberInterestRepository.deleteByMemberId(memberId);

        // 새 관심사 저장
        List<MemberInterest> interests = interestTypes.stream()
            .map(interestType -> MemberInterest.of(memberId, interestType))
            .collect(Collectors.toList());

        memberInterestRepository.saveAll(interests);
    }
}
