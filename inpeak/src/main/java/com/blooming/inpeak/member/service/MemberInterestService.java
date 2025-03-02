package com.blooming.inpeak.member.service;

import com.blooming.inpeak.member.domain.InterestType;
import com.blooming.inpeak.member.dto.response.MemberInterestResponse;
import com.blooming.inpeak.member.repository.MemberInterestRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberInterestService {

    private final MemberInterestRepository memberInterestRepository;

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
}
